package handler;

import java.lang.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Dynamic implements Handler {
	private static final String WORKING_DIR = System.getProperty("user.dir") + System.getProperty("file.separator");
	private static final String DYNAMIC_DIR =  WORKING_DIR + "dynamic";
	private static final String UPLOAD_DIR = WORKING_DIR + "temp";
	private static final boolean DEBUG = false;

	public boolean handle(Request req, DataOutputStream out) throws IOException {
		String relativePath = req.getPath();

		// Simple hack to give a filename to /
        if(relativePath.equals("/"))
			relativePath = "index";

		// String contentType = req.getHeaders().get("Content-Type");
		// if(contentType != null && contentType.contains("multipart/form-data")) {
		// 	// This is a file upload, we'll parse the file contents and save them to UPLOAD_DIR

		// 	this.handleUpload(req);

		// 	// TODO
		// }

		Path path = Paths.get(DYNAMIC_DIR + relativePath);

		// Check if the file exists in the dynamic directory
        // Also check that it's not a directory and the file is executable
		if(!Files.exists(path) || Files.isDirectory(path) || !Files.isExecutable(path)) {
			return false;
		}

		ProcessBuilder pb = new ProcessBuilder(path.toString());
		pb.redirectError(Redirect.INHERIT);

		Process p = pb.start();

		// Write the entire request to the dynamic page stdin
        BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		pout.write(req.getMethod());
		pout.newLine();
		pout.flush();
		// pout.close();
        // Read the response from the process
		ArrayList<String> resHeader = new ArrayList<String>();
		StringBuilder resBody = new StringBuilder();
		

		boolean exit = false;
		String pinStr = "";
		String resStatus = "200";
		try {
			while(!exit && pinStr != null) {
				pinStr = pin.readLine();
				if (DEBUG) System.out.println("pinStr: " + pinStr);

				switch(pinStr) {
					case "req.path":
						pout.write(req.getPath());
						pout.newLine();
						pout.flush();
						break;
					case "req.fullPath":
						pout.write(req.getFullPath());
						pout.newLine();
						pout.flush();
						break;
					case "req.header":
						String headerKey = pin.readLine();
						pout.write(req.getHeader(headerKey).trim());
						pout.newLine();
						pout.flush();
						break;
					case "req.body":
						String body = new String(req.getBody()).trim();
						String terWord = terminatingWord(body);
						pout.write(terWord); pout.newLine();
						pout.write(body); pout.newLine();
						pout.write(terWord); pout.newLine();
						pout.flush();
						break;
					case "res.status":
						resStatus = pin.readLine();
						break;
					case "res.body":
						String end = pin.readLine();
						String input = pin.readLine();
						while(!input.equals(end)) {
							resBody.append(input);
							input = pin.readLine();
						}
						break;
					case "res.header":
						resHeader.add(pin.readLine().trim());
						break;
					case "req.bodySave":
						// save request body as file
						Set<String> fileNames = new HashSet<String>();
						String contentType = req.getHeaders().get("Content-Type");
						if(contentType != null && contentType.contains("multipart/form-data")) {
							// This is a file upload, we'll parse the file contents and save them to UPLOAD_DIR
							fileNames = this.handleUpload(req);
						}
						pout.write(fileNames.toString());
						pout.newLine();
						pout.flush();
						break;
					case "res.sendFile":
						String filePath = pin.readLine();
						sendFile(filePath, out, resHeader);
						pin.close();
						pout.close();
						p.destroy();
						return true;
						// break;
					case "exit":
					case "send":
						exit = true;
						break;
					default:
						System.out.println("No case for: " + pinStr);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		pin.close();
		pout.close();
        p.destroy();

        // Send response
        out.writeBytes("HTTP/1.1 "+ resStatus +" OK\r\n");
        out.writeBytes("Content-Type: text/html\r\n");
		out.writeBytes("Content-Length: " + resBody.length() + "\r\n");
		for(String keyVal : resHeader) {
			out.writeBytes(keyVal + "\r\n");
		}
        out.writeBytes("\r\n");
		out.writeBytes(resBody.toString());
		
		return true;
	}

	// return a String that is not in output
	private String terminatingWord(String output) {
		String word;
		Random rand = new Random();

		do {
			word = Integer.toString(rand.nextInt());
		} while(output.contains(word));

		return word;
	}

	private void sendFile(String filePath, DataOutputStream out, ArrayList<String> resHeader) throws IOException {
        // If the file exists we will send the browser that data
        Path path = Paths.get(filePath);
        if(!Files.exists(path) || Files.isDirectory(path)) {
            return;
        }

        // Get the files MIME type and data
        String type = Files.probeContentType(path);
        byte[] data = Files.readAllBytes(path);
        
        out.writeBytes("HTTP/1.1 200 OK\r\n");
        
        // If our probe gave us a useful type we will set the Content-Type
        if(type != null) {
            out.writeBytes("Content-Type: " + type + "\r\n");
        }

		out.writeBytes("Content-Length: " + data.length + "\r\n");
		for(String keyVal : resHeader) {
			out.writeBytes(keyVal + "\r\n");
		}
        out.writeBytes("\r\n");
        out.write(data, 0, data.length);
        return;
	}

	public Set<String> handleUpload(Request req) {
		String boundary = req.getHeaders().get("Content-Type").split("boundary=")[1];
		
		String body = new String(req.getBody(), StandardCharsets.ISO_8859_1); 
		String[] files = body.split("--" + boundary);

		Set<String> fileNames = new HashSet<String>();
		String fileName;
		for(String section : files) {
			fileName = uploadFile(section);
			if(fileName.isEmpty())
				continue;
			fileNames.add(fileName);
		}

		return fileNames;
	}

	public String uploadFile(String file) {
		int bodySplit = file.indexOf("\r\n\r\n");

		if(bodySplit == -1)
			return "";

		String header = file.substring(0, bodySplit);
		String body = file.substring(bodySplit + 4, file.lastIndexOf("\r\n"));

		Pattern filePattern = Pattern.compile("filename=\\\"(.*)\\\"");
		Matcher matcher = filePattern.matcher(header);

		String filename = "";
		while(matcher.find()) {
			filename = matcher.group(1);
		}

		if(filename.isEmpty())
			return "";

		try {
			FileOutputStream fos = new FileOutputStream(UPLOAD_DIR + "/" + filename);
			fos.write(body.getBytes(StandardCharsets.ISO_8859_1));
			fos.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

		return filename;
	}
}

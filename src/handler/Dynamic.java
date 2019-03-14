package handler;

import java.lang.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.file.*;

public class Dynamic implements Handler {
	private static final String WORKING_DIR = System.getProperty("user.dir") + System.getProperty("file.separator");
	private static final String DYNAMIC_DIR =  WORKING_DIR + "dynamic";
	private static final String UPLOAD_DIR = WORKING_DIR + "temp";

	public boolean handle(Request req, DataOutputStream out) throws IOException {
		String relativePath = req.getPath();

		// Simple hack to give a filename to /
        if(relativePath.equals("/"))
			relativePath = "index";

		String contentType = req.getHeaders().get("Content-Type");
		if(contentType != null && contentType.contains("multipart/form-data")) {
			// This is a file upload, we'll parse the file contents and save them to UPLOAD_DIR

			this.handleUpload(req);

			// TODO
		}

		Path path = Paths.get(DYNAMIC_DIR + relativePath);

		// Check if the file exists in the dynamic directory
        // Also check that it's not a directory and the file is executable
		if(!Files.exists(path) || Files.isDirectory(path) || !Files.isExecutable(path)) {
			return false;
		}

		Process p = Runtime.getRuntime().exec(path.toString());

        // Write the entire request to the dynamic page stdin
        DataOutputStream pout = new DataOutputStream(p.getOutputStream());


		// ------------------------------------------
		//pout.writeBytes(request); // TODO: How are we sending the information to the process?
		// ------------------------------------------
		
		pout.flush();
        pout.close(); // Make sure we're closing so the dynamic page stops reading
        
        // Read the response from the process
        BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder respBuilder = new StringBuilder();       

        try {
            // Wait for the process to finish and output it's result
            p.waitFor();

            while(pin.ready()) {
                respBuilder.append((char) pin.read());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        pin.close();
        
        p.destroy();
        
        String resp = respBuilder.toString();

        // Send response
        out.writeBytes("HTTP/1.1 200 OK\r\n");
        out.writeBytes("Content-Type: text/html\r\n");
        out.writeBytes("Content-Length: " + resp.length() + "\r\n");
        out.writeBytes("\r\n");
        out.writeBytes(resp);
		
		return true;
	}

	public void handleUpload(Request req) {
		String boundary = req.getHeaders().get("Content-Type").split("boundary=")[1];
		String[] files = req.getBody().split("--" + boundary + "\r\n");

		for(String section : files) {
			uploadFile(section);
		}
	}

	public void uploadFile(String file) {
		String filename = "";
		Pattern filePattern = Pattern.compile("filename=\\\"(.*)\\\"");
		Matcher matcher = filePattern.matcher(file);

		while(matcher.find()) {
			filename = matcher.group(1);
		}

		if(filename.equals(""))
			return;

		// Short function but if we handle different file types we may need to expand this
		String cleanedFile = removeContentLines(file);
		
		try {
			FileOutputStream fos = new FileOutputStream(UPLOAD_DIR + "/" + filename);
			byte[] strToBytes = cleanedFile.getBytes();
			fos.write(strToBytes);
			fos.close();
		} catch (Exception e) {
			// TODO: Handle it or something
		}
	}

	private String removeContentLines(String file) {
		// Super naive pattern match
		file = file.replaceAll("Content.*", "");
		// Removes blank lines	
		file = file.replaceAll("(?m)^\\s+", "");
		return file;
	}
}

import java.util.*;
import java.io.*;
import java.nio.file.*;

// TODO: Cleanup
public class DynamicReqHandler extends RequestHandler {
	private static final String DYNAMIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "sites";

	public DynamicReqHandler() {}

	public boolean handle(String relativePath, String request, DataOutputStream out) throws IOException {
		if(relativePath.equals("/"))
			relativePath = "index";
		
		Path path = Paths.get(DYNAMIC_DIR + relativePath);

		// If the file exists we want to run it with the request sent to stdin.
		if(!Files.exists(path)) {
			return false;
		}

		Process p = Runtime.getRuntime().exec(path.toString());
		// Send the process the Request
		DataOutputStream pout = new DataOutputStream(p.getOutputStream());
		pout.writeBytes(request);
		pout.flush();
		pout.close();

		// Get the response from the process
		StringBuffer resp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while(true) {
			String line = in.readLine();
			if(line == null)
				break;
			resp.append(line);
		}
		//in.close();

		out.writeBytes("HTTP/1.1 200 OK\r\n");
		out.writeBytes("Content-Type: text/html\r\n");
		out.writeBytes("Content-Length: " + resp.length() + "\r\n");
		out.writeBytes("\r\n");
		out.writeBytes(resp.toString());

		//p.destroy();

		return true;
	}
}

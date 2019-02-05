import java.util.*;
import java.io.*;
import java.nio.file.*;

public class DynamicReqHandler extends RequestHandler {
    static final String DYNAMIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "sites";

    public DynamicReqHandler() {
        super("GET", "/dynamic/.*");
    }

    // Basic class to hold file information
	// Not sure if needed for dynamic files
	// as the return type will always be text (for now)
    private class TypeData {
        public String type;
        public byte[] data;
    
        public TypeData(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }

    public void handle(HashMap<String, String> reqMap, DataOutputStream out) throws IOException {
        // Get the requested path and handle the special case of root /
        String path = reqMap.get("path");

		// Third element is the one we want so we can route to C / Python
		String type = path.split("/")[2].trim();

		//TODO: Consider refactoring out some of the common code
		if (type.equals("c")) {
			String line = "";
			out.writeBytes("HTTP/1.1 200 OK\r\n");
			out.writeBytes("Content-Type: text/html\r\n");
			StringBuilder sb = new StringBuilder("");
			Process p = Runtime.getRuntime().exec(DYNAMIC_DIR + "/a.out");
			BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));  
			while ((line = input.readLine()) != null) {  
				sb.append(line);
			}  
			out.writeBytes("Content-Length: " + sb.length() + "\r\n");
			out.writeBytes("\r\n");
			out.writeBytes(sb.toString());
			input.close();  
		} else if (type.equals("python")) {
			String line = "";
			out.writeBytes("HTTP/1.1 200 OK\r\n");
			out.writeBytes("Content-Type: text/html\r\n");
			StringBuilder sb = new StringBuilder("");
			Process p = Runtime.getRuntime().exec(DYNAMIC_DIR + "/py_site.py");
			BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));  
			while ((line = input.readLine()) != null) {  
				sb.append(line);
			}  
			out.writeBytes("Content-Length: " + sb.length() + "\r\n");
			out.writeBytes("\r\n");
			out.writeBytes(sb.toString());
			input.close();  
		} else {
            out.writeBytes("HTTP/1.1 404 Not Found\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("The dynamic website could not be found.");
		}

        // Attempt to get the file type and data that is requested
        //    out.writeBytes("HTTP/1.1 200 OK\r\n");

        //    // If we know the type of the file we will send that to the browser
        //    if (source.type != null) {
        //        out.writeBytes("Content-Type: " + source.type + "\r\n");
        //    }

        //    out.writeBytes("Content-Length: " + source.data.length + "\r\n");
        //    out.writeBytes("\r\n");
        //    out.write(source.data, 0, source.data.length);
        return;
    };
}

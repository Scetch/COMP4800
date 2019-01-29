import java.util.*;
import java.io.*;
import java.nio.file.*;

public class StaticReqHandler extends RequestHandler {
    static final String STATIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "static";

    public StaticReqHandler() {
        super("GET", ".*");
    }

    // Basic class to hold file information
    private class TypeData {
        public String type;
        public byte[] data;
    
        public TypeData(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }

    // Load a static file from the STATIC_DIR and attempt to determine its type
    public TypeData loadStaticFile(String relativePath) throws IOException {
        Path path = Paths.get(STATIC_DIR + relativePath);
        if (!Files.exists(path)) {
            return null;
        }

        String type = Files.probeContentType(path);
        byte[] data = Files.readAllBytes(path);
        TypeData typeData = new TypeData(type, data);
        return typeData;
    }

    public void handle(HashMap<String, String> reqMap, DataOutputStream out) throws IOException {
        // Get the requested path and handle the special case of root /
        String path = reqMap.get("path");
        if (path.equals("/"))
            path = "/index.html";

        // Attempt to get the file type and data that is requested
        TypeData source = loadStaticFile(path);
        if (source == null) {
            out.writeBytes("HTTP/1.1 404 Not Found\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("The file was not found in the static directory.");
        } else {
            out.writeBytes("HTTP/1.1 200 OK\r\n");

            // If we know the type of the file we will send that to the browser
            if (source.type != null) {
                out.writeBytes("Content-Type: " + source.type + "\r\n");
            }

            out.writeBytes("Content-Length: " + source.data.length + "\r\n");
            out.writeBytes("\r\n");
            out.write(source.data, 0, source.data.length);
        }
        return;
    };
}
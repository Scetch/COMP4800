package handler;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

public class Static implements Handler {
    private static final String STATIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "static";

    public boolean handle(Request req, DataOutputStream out) throws IOException {
        String relativePath = req.getPath();
        
        // Simple hack to give a file name to /
        if(relativePath.equals("/"))
            relativePath = "/index.html";        
    
        // If the file exists in the static directory we will send the browser
        // that data.
        Path path = Paths.get(STATIC_DIR + relativePath);
        if(!Files.exists(path) || Files.isDirectory(path)) {
            return false;
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
        out.writeBytes("\r\n");
        out.write(data, 0, data.length);
        return true;
    }
}

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class MainServer {
    static final int PORT = 8081;
    static final String STATIC_DIR = System.getProperty("user.dir") + "/static";

    public MainServer() {}

    // Load a static file from the STATIC_DIR and attempt to determine its type
    public TypeData loadStaticFile(String relativePath) throws IOException {
        Path path = Paths.get(STATIC_DIR + relativePath);
        if(!Files.exists(path)) {
            return null;
        }

        String type = Files.probeContentType(path);
        byte[] data = Files.readAllBytes(path);
        TypeData typeData = new TypeData(type, data);
        return typeData;
    }

    // Handle a socket connection
    // Read in the request and return a response
    public void handleSocket(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Store the request line so we can get the path later and print out the entire
        // request        
        String requestLine = in.readLine();

        // Debug print the request
        System.out.println("\n");
        System.out.println(requestLine);

        String header = null;
        while(true) {
            header = in.readLine();

            if(header == null || header.isEmpty())
                break;

            System.out.println(header);
        }

        System.out.println("\n");

        // Get the requested path and handle the special case of root /
        String path = requestLine.split(" ")[1];
        if(path.equals("/"))
            path = "/index.html";

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Attempt to get the file type and data that is requested
        TypeData source = loadStaticFile(path);
        if(source == null) {
            out.writeBytes("HTTP/1.1 404 Not Found\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("The file was not found in the static directory.");
        } else {
            out.writeBytes("HTTP/1.1 200 OK\r\n");
            
            // If we know the type of the file we will send that to the browser
            if(source.type != null) {
                out.writeBytes("Content-Type: " + source.type + "\r\n");
            }

            out.writeBytes("Content-Length: " + source.type + "\r\n");
            out.writeBytes("\r\n");
            out.write(source.data, 0, source.data.length);
        }

        out.close();
        in.close();
    }

    // Run the main loop of the server
    public void run() throws IOException {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while(true) {
            // Attempt to handle a new connection.
            try {
                Socket socket = listener.accept();
                handleSocket(socket);
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // listener.close();
    }

    // Simple type to hold data about a static file
    private class TypeData {
        public String type;
        public byte[] data;

        public TypeData(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();

        try {
            server.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

import java.io.*;
import java.net.*;
import java.util.*;

import handler.Handler;

public class Server {
    private static final int PORT = 8081;
    private ArrayList<Handler> handlers;

    public Server() {
        this.handlers = new ArrayList<>();
    }

    public void addHandler(Handler handler) {
        this.handlers.add(handler);
    }

    // Bind to PORT and start listening and handling Sockets
    public void run() throws IOException {
        System.out.println("Starting server...");

        ServerSocket listener = new ServerSocket(PORT);

        System.out.println("Server started at " + InetAddress.getLocalHost() + ":" + listener.getLocalPort());

        while(true) {
            try {
                Socket socket = listener.accept();
                handleSocket(socket);
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // We never stop listening so this is dead code
        // listener.close();
    }

    // Handle a socket connection
    public void handleSocket(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
        StringBuilder reqBuilder = new StringBuilder();
        
        // Read a full request from the socket
        while(in.ready()) {
            reqBuilder.append((char) in.read());
        }
        
        String req = reqBuilder.toString();

        System.out.println("---\n" + req + "\n---");

        String[] startLine = req.split("\r\n", 2)[0].split(" ");

        // Sometimes the start-line can be empty or malformed, we'll check if it has 3 parameters
        // A start-line is in the format: method path version
        if(startLine.length == 3) {
            String method = startLine[0];
            String path = startLine[1].split("\\?", 2)[0]; // Remove a potential query from the path ?key=value&key=value
            String version = startLine[2];

            // Loop through our handlers and attempt to handle this request, falling through if
            // the handler before it did not handle the request
            for(Handler handler : handlers) {
                if(handler.handle(path, req, out)) {
                    break;
                }
            }
        }
        
        out.close();
        in.close();
    }
}

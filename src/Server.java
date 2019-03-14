import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import handler.*;

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
        
        // Wait for BufferredReader to be ready and wait for a max of 0.5 seconds
        long startTimer = System.currentTimeMillis();
        while(!in.ready()) {
            if(System.currentTimeMillis() - startTimer > 500) {
                out.close();
                in.close();
                return;
            }
        }

        String temp;
        String[] startLine;
        HashMap<String, String> headers = new HashMap<>();
        ByteArrayOutputStream bodyBuilder = new ByteArrayOutputStream();
        //StringBuilder body = new StringBuilder();
        
        // Read in startLine
        startLine = in.readLine().split(" ");

        String method = startLine[0];
        String path = startLine[1].split("\\?", 2)[0];
        
        // Read in headers
        while(in.ready()) {
            temp = in.readLine();

            // Headers are seperated from the body of the request
            // by an empty line
            if(temp.isEmpty())
                break;

            String[] parts = temp.split(": ");
            headers.put(parts[0], parts[1]);
        }

        // Read in the body
        while(in.ready()) {
            //body.append((char) in.read());
            bodyBuilder.write(in.read());
        }

        String body = bodyBuilder.toString("ISO-8859-1");

        Request req = new Request(method, path, headers, body);

        for(Handler handler : handlers) {
            if(handler.handle(req, out))
                break;
        }
        
        out.close();
        in.close();
    }
}

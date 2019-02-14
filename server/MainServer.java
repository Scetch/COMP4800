import java.io.*;
import java.net.*;
import java.util.*;

public class MainServer {
    private static final int PORT = 8081;
    private ArrayList<RequestHandler> reqHandlers;

    public MainServer(ArrayList<RequestHandler> reqHandlers) {
        this.reqHandlers = reqHandlers;
    }

    // Handle a socket connection
    // Read in the request and return a response
    public void handleSocket(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Get the full request
        StringBuilder reqBuilder = new StringBuilder();
        String headerLine = in.readLine();
        
        // Sometimes a request will be null, ignore it if so.
        if(headerLine == null) {
            return;
        }

        reqBuilder.append(headerLine);
        reqBuilder.append("\n");

        while(in.ready()) {
            reqBuilder.append((char) in.read());
        }

        String req = reqBuilder.toString();

        // TODO: Check safety of this.
        String startLine = req.split("\n", 2)[0];
        String[] parts = startLine.split(" ");
        String method = parts[0];
        String path = parts[1].split("\\?", 2)[0];

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        for(RequestHandler handler: reqHandlers) {
            if(handler.handle(path, req, out))
                break;
        }

        out.close();
        in.close();
    }

    // Run the main loop of the server
    public void run() throws IOException {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while (true) {
            // Attempt to handle a new connection.
            try {
                Socket socket = listener.accept();
                handleSocket(socket);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // listener.close();
    }

    public static void main(String[] args) {
        ArrayList<RequestHandler> reqHandlerList = new ArrayList<RequestHandler>();
        reqHandlerList.add(new DynamicReqHandler());
        reqHandlerList.add(new StaticReqHandler());
        reqHandlerList.add(new NotFoundReqHandler());

        MainServer server = new MainServer(reqHandlerList);

        try {
            server.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

import java.io.*;
import java.net.*;
import java.util.*;

public class MainServer {
    static final int PORT = 8081;
    static final String STATIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "static";
    ArrayList<RequestHandler> reqHandlerList;   // List of reqHandler (routes)

    public MainServer(ArrayList<RequestHandler> reqHandlerList) {
        this.reqHandlerList = reqHandlerList;
    }

    // Parse full path from request line in header
    // add path, query, and fragment to LinkedHashMap
    public static void parseFullpath(String fullpath, LinkedHashMap<String, String> reqMap) {
        // Get fragment string from fullpath
        int fIndex = fullpath.indexOf("#");
        if (fIndex == -1)
            fIndex = fullpath.length();

        // Get query string from fullpath
        // uses fragment index to get substring
        int qIndex = fullpath.indexOf("?");
        if (qIndex == -1)
            qIndex = fIndex;

        // Get path string from fullpath
        // uses query index to get substring
        reqMap.put("path", fullpath.substring(0, qIndex));

        // Index of query much be at same or before index of fragment
        if (qIndex <= fIndex) {
            // Add query if it's lenght is >1
            if (qIndex + 1 < fIndex)
                reqMap.put("query", fullpath.substring(qIndex + 1, fIndex));

            // Add fragment if it's lenght is >1
            if (fIndex + 1 < fullpath.length())
                reqMap.put("fragment", fullpath.substring(fIndex + 1));
        }

        return;
    }

    // Handle a socket connection
    // Read in the request and return a response
    public void handleSocket(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        LinkedHashMap<String, String> reqMap = new LinkedHashMap<String, String>();

        // Timeout
        long startTime = System.currentTimeMillis();

        // Wait for BufferedReader or wait for 2.5 seconds
        while (!in.ready()) {
            if (System.currentTimeMillis() > startTime + 2500) {
                in.close();
                return;
            }
        };

        // Store the request line so we can get the path later and print out the entire
        // request
        String[] requestLine = in.readLine().split(" ");
        if (requestLine.length == 3) {
            reqMap.put("method", requestLine[0]);
            reqMap.put("fullpath", requestLine[1]);
            reqMap.put("protocol", requestLine[2]);
        } else {
            // Handle error in request line
        }

        // Parse full path to get query and/or fragment if present
        parseFullpath(reqMap.get("fullpath"), reqMap);

        // Read the rest of the header and store info into reqMap
        String headerLine = in.readLine();
        while (in.ready()) {
            headerLine = in.readLine();
            // After CRLF in header read body
            if (headerLine.isEmpty() && in.ready()) {
                StringBuilder sb = new StringBuilder();
                while (in.ready()) {
                    sb.append((char) in.read());
                }
                reqMap.put("body", sb.toString());
                break;
            }

            // Parse header line, and store into reqMap
            int indexCol = headerLine.indexOf(":");
            if (indexCol + 1 < headerLine.length()) {
                reqMap.put(headerLine.substring(0, indexCol),
                        headerLine.substring(indexCol + 1, headerLine.length()).trim());
            }
        }

        // Debug print entire LinkedHashMap
        for (String key : reqMap.keySet()) {
            System.out.println(key + ": " + reqMap.get(key));
        }
        System.out.println("\n");

        // Response stream
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Go through request handlers until one matches
        for(RequestHandler handler: reqHandlerList) {
            // check is request matches the handler
            if (reqMap.get("method").matches(handler.method) && reqMap.get("path").matches(handler.pathRegx)) {
                handler.handle(reqMap, out);
                break;
            }
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
        // Add request handler (order matters)
        reqHandlerList.add(new MarkReqHandler());
        reqHandlerList.add(new StaticReqHandler());

        MainServer server = new MainServer(reqHandlerList);

        try {
            server.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

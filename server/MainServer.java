import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class MainServer {
    static final int PORT = 8081;
    static final String STATIC_DIR = System.getProperty("user.dir") + "/static";

    public static String loadStaticFile(String path) throws IOException {
        File file = new File(STATIC_DIR + path);
        if(!file.exists()) {
            return "";
        }

        byte[] encoded = Files.readAllBytes(Paths.get(STATIC_DIR + path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            ServerSocket listener = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while(true) {
                Socket socket = listener.accept();

                if(socket == null) {
                    break;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header = in.readLine();
                String[] split = header.split(" ");

                // The path / is a special case as it will also look for index.html.
                String path = split[1];
                if(path.equals("/")) {
                    path = "/index.html";
                }

                String source = loadStaticFile(path);

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("Content-Length: " + source.length() + "\r\n");
                out.write("\r\n");
                out.write(source);
                out.flush();

                out.close();
                in.close();
                socket.close();
            }

            listener.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

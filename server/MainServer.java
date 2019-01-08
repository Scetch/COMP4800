import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class MainServer { 
    // port to listen connection
    static final int PORT = 8081;
    
    // Client Connection via Socket Class
    private Socket connect;
    
    public MainServer(Socket c) {
        connect = c;
    }
    
    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
            
            // we listen until user halts server execution
            while (true) {
                MainServer myServer = new MainServer(serverConnect.accept());
				myServer.run();
            }
            
        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

	public void run()
	{
		try {
			BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			// get first line of the request from the client
			String input = in.readLine();

			String response = "<html>Hello from our server!</html>";

			System.out.println("Got this: " + input + "\n");
			out.println("HTTP/1.1 200 OK");
			out.println("Server: Cool Server 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: text/html");
			out.println("Content-length: " + response.length());
			out.println(); // blank line between headers and content, very important !
			out.println(response);
			out.flush(); // flush character output stream buffer
		} catch (IOException e) {
			System.err.println("Error running the server: " + e.getMessage());
		}
	}
}

import java.util.ArrayList;

import handler.*;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server();

            // Add the request handlers our server will use
            server.addHandler(new Dynamic());
            server.addHandler(new Static());
            server.addHandler(new NotFound());

            // Run the server
            server.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

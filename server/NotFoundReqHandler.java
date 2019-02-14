import java.io.*;

public class NotFoundReqHandler extends RequestHandler {
    private static final String MESSAGE = "The requested path was not found.";

    public NotFoundReqHandler() {}

    // The NotFoundReqHandler will always return a Response.
    public boolean handle(String path, String request, DataOutputStream out) throws IOException {
        out.writeBytes("HTTP/1.1 404 Not Found\r\n");
        out.writeBytes("Content-Type: text/plain\r\n");
        out.writeBytes("Content-Length: " + MESSAGE.length() + "\r\n");
        out.writeBytes("\r\n");
        out.writeBytes(MESSAGE);
        return true;
    }
}
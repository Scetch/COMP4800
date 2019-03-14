package handler;

import java.io.*;

public class NotFound implements Handler {
    private static final String MESSAGE = "The requested path was not found.";

    public boolean handle(Request req, DataOutputStream out) throws IOException {
        out.writeBytes("HTTP/1.1 404 Not Found\r\n");
        out.writeBytes("Content-Type: text/plain\r\n");
        out.writeBytes("Content-Length: " + MESSAGE.length() + "\r\n");
        out.writeBytes("\r\n");
        out.writeBytes(MESSAGE);
        return true;
    }
}
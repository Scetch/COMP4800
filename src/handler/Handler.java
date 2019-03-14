package handler;

import java.io.*;

public interface Handler {
    public boolean handle(Request req, DataOutputStream out) throws IOException;
}
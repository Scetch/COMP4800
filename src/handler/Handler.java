package handler;

import java.io.*;

public interface Handler {
    public boolean handle(String path, String request, DataOutputStream out) throws IOException;
}
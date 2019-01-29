import java.util.*;
import java.io.*;

public abstract class RequestHandler {
    public String method;
    public String pathRegx;

    public RequestHandler(String method, String path) {
        this.method = method;   // GET, POST etc.
        this.pathRegx = path;   // Regex used to check is paths match
    }

    // Called to handle the request, given DataOutputStream to response to request
    public abstract void handle(HashMap<String, String> reqMap, DataOutputStream out) throws IOException;

}
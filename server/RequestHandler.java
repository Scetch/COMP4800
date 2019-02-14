import java.util.*;
import java.io.*;

public abstract class RequestHandler {
    public RequestHandler() {}

    /*
    public String method;
    public String pathRegx;

    public RequestHandler(String method, String path) {
        this.method = method;   // GET, POST etc.
        this.pathRegx = path;   // Regex used to check is paths match
    }
    */

    /*
        Given a Request, return a Response. Null will determine if the handler
        could handle this type of file.
    */
    public abstract boolean handle(String path, String request, DataOutputStream out) throws IOException;
}
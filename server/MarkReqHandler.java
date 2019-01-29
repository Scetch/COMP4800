import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

public class MarkReqHandler extends RequestHandler {
    // Path to marks folder
    static final String STATIC_DIR = System.getProperty("user.dir") + System.getProperty("file.separator") + "marks";

    public MarkReqHandler() {
        // Handle both GET and POST request for /marks path
        super("GET|POST", "/marks");
    }

    private void returnRequest(DataOutputStream out) throws IOException {
        Path marksPath = Paths.get(STATIC_DIR + "/marks.txt");
        Path classPath = Paths.get(STATIC_DIR + "/classlist.txt");
        List<String> marksLines = Files.readAllLines(marksPath, Charset.forName("UTF8"));
        List<String> classLines = Files.readAllLines(classPath, Charset.forName("UTF8"));

        out.writeBytes("HTTP/1.1 200 OK\r\n");
        out.writeBytes("Content-Type: text/html\r\n");
        out.writeBytes("\r\n");
        // Web page
        out.writeBytes("<!DOCTYPE html>\r\n <html>\r\n <head>\r\n <title>Marks Page</title>\r\n </head>\r\n <body>\r\n <div style=\"text-align: center;\">\r\n <h2>Marks page.</h2>\r\n <a href=\"/\">Back</a>\r\n <form action=\"/marks\" method=\"post\">\r\n");
        if(marksLines.size() == classLines.size()) {
            String name;
            String mark;
            for(int i=0; i < marksLines.size(); i++) {
                name = classLines.get(i);
                mark = marksLines.get(i);
                out.writeBytes(String.format("%1$s: <input type=\"text\" name=\"%3$d\" value=\"%2$s\"> <br><br>", name, mark, i));
            }
        }

        out.writeBytes("<input type=\"submit\" value=\"Submit\">\r\n </form>\r\n </div>\r\n </body>\r\n</html>");
    };

    // Parse query from request
    // e.g. "title=Query_string&action=edit"
    private static LinkedHashMap<String,String> parseQuery(String query) {
        LinkedHashMap<String,String> queryMap = new LinkedHashMap<String,String>();
        // Each field value pair is separted by a '&' or ';'
        // the field and value are separted by an '='
        for(String pair : query.split("&|;")) {
            String[] split = pair.split("=",2);
            queryMap.put(split[0], split[1]);
        }
        return queryMap;
    }

    public void handle(HashMap<String, String> reqMap, DataOutputStream out) throws IOException {
        if (reqMap.get("method").equals("GET")) {           // GET request
            returnRequest(out);            
        } else if (reqMap.get("method").equals("POST")) {   // POST request
            // Parse query
            LinkedHashMap<String, String> marksMap = parseQuery(reqMap.get("body"));

            // Create list of new marks
            ArrayList<String> newMarks = new ArrayList<String>();
            for (String key : marksMap.keySet())
                newMarks.add(marksMap.get(key));

            // Write/save new marks
            Path marksPath = Paths.get(STATIC_DIR + "/marks.txt");
            Files.write(marksPath, newMarks, Charset.forName("UTF8"));

            returnRequest(out);
        } else { // other request 
            out.writeBytes("HTTP/1.1 404 Not Found\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("ERROR!");
        }

    };
}
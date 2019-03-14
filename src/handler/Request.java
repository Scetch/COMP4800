package handler;

import java.util.HashMap;

public class Request {
    private String method;
    private String path;
    private HashMap<String, String> headers;
    private String body;

    public Request(String method, String path, HashMap<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public String getBody() {
        return this.body;
    }
}
package handler;

import java.util.HashMap;

public class Request {
    private String method;
    private String path;
    private HashMap<String, String> headers;
    private byte[] body;

    public Request(String method, String path, HashMap<String, String> headers, byte[] body) {
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

    public byte[] getBody() {
        return this.body;
    }
}
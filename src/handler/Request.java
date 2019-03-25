package handler;

import java.util.HashMap;

public class Request {
    private String method;
    private String fullPath;
    private String path;
    private HashMap<String, String> headers;
    private byte[] body;

    public Request(String method, String fullPath, HashMap<String, String> headers, byte[] body) {
        this.method = method;
        this.fullPath = fullPath;
        this.path = fullPath.split("\\?", 2)[0];
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return this.method;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public String getPath() {
        return this.path;
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public String getHeader(String key) {
        String value = this.headers.get(key);
        return (value != null) ? value : "";
    }

    public byte[] getBody() {
        return this.body;
    }
}
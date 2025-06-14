package com.example.http;

import java.util.Map;


public class HttpRequest implements HttpMessage{

    private HttpMethod method;
    private String path;
    private String version;
    private Map<String, String> headers; //Talvez criar um enum de headers
    private byte[] body;

    public HttpRequest(){
        
    }

    public HttpRequest(HttpMethod method, String path){
        setMethod(HttpMethod.GET);
        setVersion("HTTP/1.1");
        setPath(path);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }
    

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;

        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpRequest setBody(byte[] body) {
        this.body = body;

        return this;
    }

    public void print() {
        System.out.println(method + " " + path + " " + version);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        System.out.println(); // linha em branco separando cabeçalhos do corpo

        if (body != null && body.length > 0) {
            System.out.println(body);
        }
    }

    public String getContentType(){

        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".json")) return "application/json";

        return "application/octet-stream";
    }

}

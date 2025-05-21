package com.example.http;

import java.util.LinkedList;
import java.util.Map;

import com.example.parser.HttpParser;

public class HttpMessage {

    private HttpMethod method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private String body;

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

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void print() {
        System.out.println(method + " " + path + " " + version);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        System.out.println(); // linha em branco separando cabeçalhos do corpo

        if (body != null && !body.isEmpty()) {
            System.out.println(body);
        }
    }

}

package com.example.http;

public class HttpRequest extends HttpMessage{
    
    private HttpMethod method;
    private String requestTarget;
    private String httpVersion;

    HttpRequest(HttpMethod method, String requestTarget, String httpVersion){
        this.method = method;
        this.requestTarget = requestTarget;
        this.httpVersion = httpVersion;
    }
}

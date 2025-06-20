package com.example.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse implements HttpMessage{

    private HttpStatusCode status;
    private String version;
    private Map<String, String> headers;
    private byte[] body;
    
    private final String CRLF = "\r\n";

    public String toString(){


        String responseRaw =  version +" "+ status.STATUS_CODE + CRLF;

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                responseRaw = responseRaw.concat(entry.getKey() + ": " + entry.getValue()).concat(CRLF);
            }
        }

        return responseRaw.concat(CRLF);

    }

    public HttpResponse setStatusCode(int code) {
        this.status = HttpStatusCode.fromCode(code);

        return this;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public HttpResponse setVersion(String version) {
        this.version = version;

        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpResponse addHeader(String name, String content){
        this.headers.put(name, content);

        return this;
    }

    public HttpResponse setHeaders(Map<String, String> headers) {
        this.headers = headers;

        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpResponse setBody(byte[] body) {
        this.body = body;

        return this;
    }

    //Transformar content type em enum?
    public static HttpResponse OK(byte[] body, String contentType){

        var headers = new HashMap<String,String>();

        headers.put("Content-Length", "" + body.length);

        if(contentType != null){
            headers.put("Content-Type", contentType);
        }

         HttpResponse response =  new HttpResponse()
                                    .setStatusCode(200)
                                    .setVersion("HTTP/1.1")
                                    .setHeaders(headers)
                                    .setBody(body);

        return response;
    }
}

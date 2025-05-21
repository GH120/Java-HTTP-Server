package com.example.http;

public class HttpParseException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public HttpParseException(HttpStatusCode statusCode) {
        super(statusCode.MESSAGE);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
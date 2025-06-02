package com.example.parser;

import com.example.http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HttpStreamWriter {
    
    static public void send(HttpResponse response, OutputStream output) throws IOException {

        Objects.requireNonNull(response, "HTTP response cannot be null");
        Objects.requireNonNull(output, "Output stream cannot be null");

        // Escreve status line
        String header = response.toString();

        output.write(header.getBytes());
        
        // Escreve body se existir
        if (response.getBody() != null) {
            output.write(response.getBody());
        }
        
        output.flush();
    }
    
    static public void writeErrorResponse(int statusCode, String message, OutputStream output) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setVersion("HTTP/1.1");
        response.setStatusCode(statusCode);
        // response.setStatusMessage(message);
        response.addHeader("Content-Type", "text/plain");
        response.setBody(message.getBytes(StandardCharsets.UTF_8));
        
        send(response, output);
    }
}
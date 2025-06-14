package com.example.http;

import java.util.Map;


public interface HttpMessage {

    HttpMessage         setHeaders(Map<String, String> map);
    Map<String, String> getHeaders();
    HttpMessage         setBody(byte[] body);
    byte[]              getBody();
}

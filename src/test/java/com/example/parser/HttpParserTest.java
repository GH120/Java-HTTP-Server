package com.example.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedList;

import com.example.http.HttpMessage;
import com.example.http.HttpParseException;
import com.example.http.HttpStatusCode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HttpParserTest {

    private static HttpLexer lexer;
    private static HttpParser httpParser;
    private static HttpStreamReader httpBuilder;

    @BeforeAll
    public static void beforeClass() {
        lexer = new HttpLexer();
        httpParser = new HttpParser();
        httpBuilder = new HttpStreamReader();
    }

    
    @Test
    void testInvalidRequestLine_missingMethod() {
        String input = "/ HTTP/1.1\r\nHost: localhost\r\n\r\n";
        LinkedList<Token> tokens = new LinkedList<>(lexer.tokenize(input));

        assertThrows(HttpParseException.class, () -> {
            httpParser.parse(tokens);
        });
    }

    @Test
    void testInvalidRequestLine_unsupportedMethod() {
        String input = "FETCH / HTTP/1.1\r\nHost: localhost\r\n\r\n";
        LinkedList<Token> tokens = new LinkedList<>(lexer.tokenize(input));

        HttpParseException ex = assertThrows(HttpParseException.class, () -> {
            httpParser.parse(tokens);
        });

        assert ex.getStatusCode() == HttpStatusCode.CLIENT_ERROR_401_METHOD_NOT_ALLOWED;
    }

    @Test
    void testInvalidRequestLine_missingPath() {
        String input = "GET  HTTP/1.1\r\nHost: localhost\r\n\r\n";
        LinkedList<Token> tokens = new LinkedList<>(lexer.tokenize(input));

        HttpParseException ex = assertThrows(HttpParseException.class, () -> {
            httpParser.parse(tokens);
        });

        assert ex.getStatusCode() == HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST;
    }

    @Test
    void testInvalidRequestLine_missingVersion() {
        String input = "GET /\r\nHost: localhost\r\n\r\n";
        LinkedList<Token> tokens = new LinkedList<>(lexer.tokenize(input));

        HttpParseException ex = assertThrows(HttpParseException.class, () -> {
            httpParser.parse(tokens);
        });

        assert ex.getStatusCode() == HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST;
    }

    @Test
    void testInvalidRequestLine_pathTooLong() {
        StringBuilder longPath = new StringBuilder("/");
        for (int i = 0; i < 2049; i++) longPath.append("a");

        String input = "GET " + longPath + " HTTP/1.1\r\nHost: localhost\r\n\r\n";
        LinkedList<Token> tokens = new LinkedList<>(lexer.tokenize(input));

        HttpParseException ex = assertThrows(HttpParseException.class, () -> {
            httpParser.parse(tokens);
        });

        assert ex.getStatusCode() == HttpStatusCode.CLIENT_ERROR_414_BAD_REQUEST;
    }

    private String generateValidTestCase() {
        return "GET / HTTP/1.1\r\n" +
               "Host: localhost:8080\r\n" +
               "Connection: keep-alive\r\n" +
               "Cache-Control: max-age=0\r\n" +
               "sec-ch-ua: \"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"\r\n" +
               "sec-ch-ua-mobile: ?0\r\n" +
               "sec-ch-ua-platform: \"Windows\"\r\n" +
               "Upgrade-Insecure-Requests: 1\r\n" +
               "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36\r\n" +
               "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" +
               "Sec-Fetch-Site: none\r\n" +
               "Sec-Fetch-Mode: navigate\r\n" +
               "Sec-Fetch-User: ?1\r\n" +
               "Sec-Fetch-Dest: document\r\n" +
               "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
               "Accept-Language: pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7\r\n";
    }
}

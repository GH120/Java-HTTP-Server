package com.example.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HttpParserTest {

    private HttpLexer lexer;
    private HttpParser httpParser;

    @BeforeAll
    public void beforeClass(){

        lexer = new HttpLexer();
        httpParser = new HttpParser();
    }

    @Test
    void testParse() {
        httpParser.parse(new LinkedList<>(lexer.parse(generateValidTestCase())));
    }

    private String generateValidTestCase(){

        String rawData = "GET / HTTP/1.1\r\n" + //
                        "Host:Connection accepted/0:0:0:0:0:0:0:1\r\n" + //
                        " localhost:8080\r\n" + //
                        "Connection: keep-alive\r\n" + //
                        "Cache-Control: max-age=0\r\n" + //
                        "sec-ch-ua: \"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"\r\n" + //
                        "sec-ch-ua-mobile: ?0\r\n" + //
                        "sec-ch-ua-platform: \"Windows\"\r\n" + //
                        "Upgrade-Insecure-Requests: 1\r\n" + //
                        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36\r\n" + //
                        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" + //
                        "Sec-Fetch-Site: none\r\n" + //
                        "Sec-Fetch-Mode: navigate\r\n" + //
                        "Sec-Fetch-User: ?1\r\n" + //
                        "Sec-Fetch-Dest: document\r\n" + //
                        "Accept-Encoding: gzip, deflate, br, zstd\r\n" + //
                        "Accept-Language: pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7\r\n"; //tem que ter o \r\n no final

            return rawData;
    }
}

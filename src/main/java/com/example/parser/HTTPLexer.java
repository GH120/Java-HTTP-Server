package com.example.parser;

import java.util.*;
// import java.util.regex.*;

//Transformar regras em ENUM?
public class HttpLexer extends Lexer{

    public static void main(String[] args) {

        Lexer lexer = new HttpLexer();
        lexer.tokenize(lexer.testCase());
    }

    public HttpLexer(){
        super();

        //Obs: ordem de inserção importa. Em caso de empate, a primeira inserida é a escolhida
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("GET", "GET");
        rules.put("PUT", "PUT");
        rules.put("HEAD", "HEAD");
        rules.put("POST", "POST");
        rules.put("DELETE", "DELETE");
        rules.put("CONNECT", "CONNECT");
        rules.put("OPTIONS", "OPTIONS");
        rules.put("TRACE", "TRACE");
        rules.put("CRLF", "(\\r\\n)+");
        rules.put("SPACE", "(\\s)+");
        rules.put("VERSION", "HTTP/[0-9]\\.[0-9]");
        rules.put("BAR", "/");
        rules.put("LOCALHOST", "localhost");
        rules.put("NUMBER", "[0-9]+");
        rules.put("EQUALS", "=");
        rules.put("HEADER_NAME", 
            "Host|Origin|User-Agent|Accept|Purpose|Content-Type|Content-Length|Connection|Authorization|Cache-Control|Set-Cookie|Date|Server|Referer"
            + "|(S|s)(e|E)c(-[a-zA-Z0-9]+)+"  // pega headers tipo Sec-*, sec-*
            + "|(S|s)(c|C)p(-[a-zA-Z0-9]+)*"  // pega headers tipo Scp-*
            + "|(X|x)-[a-zA-Z0-9\\-]+"        // headers não-padrão como X-Forwarded-For
        );
        rules.put("NON_STANDARD_HEADER", "(([a-zA-Z0-9]+\\-)+([a-zA-Z0-9)]+))");
        rules.put("WORD", "[a-zA-Z0-9\\-_.]+"); // simples e abrangente
        rules.put("DOT", "\\.");
        rules.put("COLON", ":");
        rules.put("ADDRESS", "([0-9a-fA-F]{1,4}:){2,7}[0-9a-fA-F]{1,4}"); // IPv6 básico
        rules.put("SYMBOL", "[;\\+\\*,\\?!{}\\[\\]]");
        rules.put("LPAR", "\\(");
        rules.put("RPAR", "\\)");
        rules.put("LBRACKET", "\\[");
        rules.put("RBRACKET", "\\]");
        rules.put("QUOTES", "\"");

        this.rules = this.toRegex(rules);
        this.separators = Arrays.asList(",");
    }

    public String testCase(){

        return "GET / HTTP/1.1\r\n" + //
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
    }
}
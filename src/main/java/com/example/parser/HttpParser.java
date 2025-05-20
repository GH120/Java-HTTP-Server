package com.example.parser;

import java.util.Collections;
import java.util.LinkedList;


public class HttpParser {

    LinkedList<Token> tokens;

    Token eat(String expected) throws Exception {
        Token token = tokens.pop();
        if (token.type.equals(expected)) {
            return token;
        } else {
            throw new Exception("Esperado: " + expected + " mas encontrado: " + token.type);
        }
    }

    public void parse(LinkedList<Token> tokens) {
        Collections.reverse(tokens);
        this.tokens = tokens;

        try {
            REQUEST_LINE();
            HEADERS();
            BODY(); // opcional
            System.out.println("Parsing concluído com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro no parsing: " + e.getMessage());
        }
    }

    void REQUEST_LINE() throws Exception {
        eat("METHOD");       // Ex: GET
        eat("SPACE");
        eat("PATH");         // Ex: /
        eat("SPACE");
        eat("VERSION");      // Ex: HTTP/1.1
        eat("CRLF");
    }

    void HEADERS() throws Exception {
        while (!tokens.isEmpty()) {
            if (tokens.peek().type.equals("CRLF")) {
                eat("CRLF"); // fim da seção de cabeçalhos
                break;
            }

            eat("HEADER_NAME");   // Ex: Host
            eat("COLON");         // :
            while (!tokens.peek().type.equals("CRLF")) {
                eat("HEADER_VALUE"); // valores podem ser divididos em múltiplos tokens
            }
            eat("CRLF");
        }
    }

    void BODY() throws Exception {
        while (!tokens.isEmpty()) {
            eat("BODY");
        }
    }
}

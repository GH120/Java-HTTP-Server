package com.example.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class TreeBuilder{

    private TreeNode   tree;
    private TreeNode   currentNode;
    private LinkedList<TreeNode> ancestors;

    public TreeBuilder(){
        tree        = new TreeNode("ROOT");
        currentNode = tree;
        ancestors   = new LinkedList<>();
    }

    public void startContext(String name){

        TreeNode parent = currentNode;

        ancestors.push(parent);

        currentNode = new TreeNode(name);

        parent.children.add(currentNode);
    }

    public void endContext(){

        TreeNode parent = ancestors.poll();
        
        currentNode = parent;
    }

    public TreeNode getTree(){
        return tree;
    }
}

public class HttpParser {

    LinkedList<Token> tokens;
    TreeBuilder treeBuilder = new TreeBuilder();

    public static void main(String[] args) {
        Lexer lexer = new HttpLexer();

        List<Token> tokens = lexer.parse(lexer.testCase());

        HttpParser parser = new HttpParser();

        parser.parse(new LinkedList<Token>(tokens));

        // Exibe a árvore final (opcional)
        System.out.println(parser.treeBuilder.getTree());
    }

    private Token eat(String expected) throws Exception {
        Token token = tokens.poll();
        if (token.type.equals(expected)) {
            return token;
        } else {
            System.out.println(token);
            throw new Exception("Esperado: " + expected + " mas encontrado: " + token.type);
        }
    }

    public void parse(LinkedList<Token> tokens) {
        this.tokens = tokens;

        try {
            treeBuilder.startContext("HTTP_MESSAGE");
            REQUEST_LINE();
            // HEADERS();
            // BODY(); // opcional
            treeBuilder.endContext();
            System.out.println("Parsing concluído com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro no parsing: " + e.getMessage());
        }
    }

    void REQUEST_LINE() throws Exception {
        treeBuilder.startContext("REQUEST_LINE");
        METHOD();
        eat("SPACE");
        PATH();
        eat("SPACE");
        eat("VERSION");
        // eat("CRLF");
        treeBuilder.endContext();
    }

    void METHOD() throws Exception {
        treeBuilder.startContext("METHOD");
        switch (tokens.peek().type) {
            case "GET": eat("GET"); break;
            case "PUT": eat("PUT"); break;
            case "UPDATE": eat("UPDATE"); break;
            case "DELETE": eat("DELETE"); break;
            case "TRACE": eat("TRACE"); break;
            case "OPTIONS": eat("OPTIONS"); break;
            case "CONNECT": eat("CONNECT"); break;
            case "HEAD": eat("HEAD"); break;
        }
        treeBuilder.endContext();
    }

    void HEADERS() throws Exception {
        treeBuilder.startContext("HEADERS");
        while (!tokens.isEmpty()) {
            if (tokens.peek().type.equals("CRLF")) {
                eat("CRLF"); // fim da seção de cabeçalhos
                break;
            }
            parseSingleHeader();
        }
        treeBuilder.endContext();
    }

    void parseSingleHeader() throws Exception {
        treeBuilder.startContext("HEADER");
        Token headerNameToken = eat("HEADER_NAME");
        String headerName = headerNameToken.string.toLowerCase();

        eat("COLON");

        StringBuilder sb = new StringBuilder();
        while (!tokens.peek().type.equals("CRLF")) {
            sb.append(eat("HEADER_VALUE").string);
        }
        String value = sb.toString().trim();

        eat("CRLF");

        handleSingleHeader(headerName, value);
        treeBuilder.endContext();
    }

    void handleSingleHeader(String headerName, String value) {
        treeBuilder.startContext("HEADER_HANDLER:" + headerName);
        switch (headerName) {
            case "host":
                System.out.println("Host: " + value);
                break;
            case "connection":
                System.out.println("Connection: " + value);
                break;
            case "cache-control":
                System.out.println("Cache-Control: " + value);
                break;
            default:
                System.out.println("Header desconhecido (" + headerName + "): " + value);
                break;
        }
        treeBuilder.endContext();
    }

    String PATH() throws Exception {
        treeBuilder.startContext("PATH");
        StringBuilder path = new StringBuilder();

        while (!tokens.isEmpty()) {
            Token token = tokens.peek();

            switch (token.type) {
                case "BAR":
                case "DOT":
                case "WORD":
                case "NUMBER":
                    path.append(eat(token.type).string);
                    break;
                default:
                    treeBuilder.endContext();
                    return path.toString();
            }
        }

        treeBuilder.endContext();
        return path.toString();
    }

    void BODY() throws Exception {
        treeBuilder.startContext("BODY");
        // Placeholder para parsing do corpo
        treeBuilder.endContext();
    }
}

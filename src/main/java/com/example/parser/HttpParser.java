package com.example.parser;

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

    public void insertToken(Token token){
        currentNode.children.add(token);
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
    }

    private Token eat(String expected) throws Exception {
        Token token = tokens.poll();

        if (token.type.equals(expected)) {

            treeBuilder.insertToken(token);

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
            HEADERS();
            // BODY(); // opcional
            treeBuilder.endContext();
            // Exibe a árvore final (opcional)
            System.out.println(treeBuilder.getTree());
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
        eat("CRLF");
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

        if(tokens.peek().type.equals("HEADER_NAME")){
            eat("HEADER_NAME");
        }
        else{
            eat("NON_STANDARD_HEADER");
        }

        eat("COLON"); 
        HEADER_VALUE();
        eat("CRLF");

        treeBuilder.endContext();
    }

    void HEADER_VALUE() throws Exception{
        treeBuilder.startContext("HEADER_VALUE");

        while (!tokens.peek().type.equals("CRLF")) {
            eat(tokens.peek().type); //Come qualquer token
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
                    path.append(eat(token.type).expression);
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

    public TreeNode getTree(){
        return treeBuilder.getTree();
    }
}

package com.example.parser;

import java.util.LinkedList;
import java.util.List;

import com.example.http.HttpParseException;
import com.example.http.HttpStatusCode;

public class HttpParser {

    LinkedList<Token> tokens;
    TreeBuilder treeBuilder = new TreeBuilder();

    public void parse(LinkedList<Token> tokens) {

        this.tokens = tokens;

        try {
            treeBuilder.startContext("HTTP_MESSAGE"); 

            //Símbolos não terminais são métodos recursivos
            REQUEST_LINE();
            HEADERS();
            // BODY(); // opcional

            treeBuilder.endContext();
            
            System.out.println("Parsing concluído com sucesso.");
        } catch (HttpParseException e) {
            System.err.println("Erro HTTP " + e.getStatusCode().STATUS_CODE + ": " + e.getStatusCode().MESSAGE);

            throw e;
        } catch (Exception e) {
            System.err.println("Erro no parsing: " + e.getMessage());
        }
    }

    //CONSOME UM TOKEN (TERMINAL) SE ELE FOR DO TIPO ESPERADO, SENÃO PARA EXECUÇÃO 
    private Token eat(String expected) throws Exception {

        if (tokens.isEmpty()) throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);

        Token token = tokens.poll();

        if (token.type.equals(expected)) {
            treeBuilder.insertToken(token);
            return token;
        } else 
            throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        
    }

    /**********************************************************/
    /*MÉTODOS DOS SÍMBOLOS NÃO TERMINAIS DA GRAMÁTICA DO HTTP */
    /**********************************************************/
    void REQUEST_LINE() throws Exception {
        treeBuilder.startContext("REQUEST_LINE");

        if (!isValidMethod(tokens.peek())) {
            throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_401_METHOD_NOT_ALLOWED);
        }
        METHOD();

        eat("SPACE");

        String path = PATH();
        if (path.length() > 2048) {
            throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_414_BAD_REQUEST);
        }

        eat("SPACE");

        Token versionToken = tokens.peek();
        if (versionToken == null || !versionToken.type.equals("VERSION")) {
            throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }
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
            case "POST": eat("POST"); break;
            default:
                throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_401_METHOD_NOT_ALLOWED);
        }
        treeBuilder.endContext();
    }

    void HEADERS() throws Exception {
        treeBuilder.startContext("HEADERS");
        while (!tokens.isEmpty()) {
            if (tokens.peek().type.equals("CRLF")) {
                eat("CRLF");
                break;
            }
            HEADER();
        }
        treeBuilder.endContext();
    }

    void HEADER() throws Exception {
        treeBuilder.startContext("HEADER");

        switch (tokens.peek().type) {
            case "HEADER_NAME", "NON_STANDARD_HEADER"-> {eat(tokens.peek().type);}

            default -> {
                System.out.println("Token lido: " + tokens.peek() + " Token esperado: " + "HEADER_NAME");
                throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
            }
        }

        eat("COLON");
        HEADER_VALUE();
        eat("CRLF");

        treeBuilder.endContext();
    }

    void HEADER_VALUE() throws Exception {
        treeBuilder.startContext("HEADER_VALUE");

        while (!tokens.isEmpty() && !tokens.peek().type.equals("CRLF")) {
            eat(tokens.peek().type);
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
        treeBuilder.endContext();
    }

    //*********************************************************************/
    //**************************Métodos Auxiliares*************************/
    //*********************************************************************/
    public TreeNode getTree() {
        return treeBuilder.getTree();
    }

    private boolean isValidMethod(Token token) {
        if (token == null) return false;

        return switch (token.type) {
            case "GET", "PUT", "UPDATE","POST", "DELETE", "TRACE", "OPTIONS", "CONNECT", "HEAD" -> true;
            default -> false;
        };
    }
}

//Serve para auxiliar na construção de uma árvore na medida que percorre o recursive descent
//Ao entrar em um Não terminal (método em maiúsculo), adiciona um nó a árvore 
//Toda adição de tokens serão nós filhos desse último nó.
//Outros não terminais chamados antes de sair do contexto serão filhos diretos desse nó
//Ao sair do contexto, volta para o pai dele 
class TreeBuilder {

    private TreeNode tree;
    private TreeNode currentNode;
    private LinkedList<TreeNode> ancestors;

    public TreeBuilder() {
        tree = new TreeNode("ROOT");
        currentNode = tree;
        ancestors = new LinkedList<>();
    }

    public void startContext(String name) {
        TreeNode parent = currentNode;
        ancestors.push(parent);
        currentNode = new TreeNode(name);
        parent.children.add(currentNode);
    }

    public void insertToken(Token token) {
        currentNode.children.add(token);
    }

    public void endContext() {
        TreeNode parent = ancestors.poll();
        currentNode = parent;
    }

    public TreeNode getTree() {
        return tree;
    }

    //Teste
    public static void main(String[] args) {
        Lexer lexer = new HttpLexer();
        List<Token> tokens = lexer.tokenize(lexer.testCase());
        HttpParser parser = new HttpParser();
        parser.parse(new LinkedList<Token>(tokens));
    }
}
package com.example.parser;

import com.example.http.HttpMessage;
import com.example.http.HttpMethod;
import com.example.http.HttpParseException;
import com.example.http.HttpStatusCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpBuilder {

    private HttpMessage message;

    public HttpBuilder() {
        this.message = new HttpMessage();
    }

    public HttpMessage build(TreeNode AST) {
        extractRequestLine(AST);
        extractHeaders(AST);
        extractBody(AST);
        return message;
    }

    private void extractRequestLine(TreeNode AST) {
        TreeNode requestLine = (TreeNode) AST.getNodeByType("REQUEST_LINE").get(0);

        try{

            String methodStr = requestLine.getNodeByType("METHOD").get(0).getExpression();
            String path = requestLine.getNodeByType("PATH").get(0).getExpression();
            String version = requestLine.getNodeByType("VERSION").get(0).getExpression();

            HttpMethod method; // Assumindo enum HttpMethod

            try{
                method = HttpMethod.valueOf(methodStr);
            } catch(IllegalArgumentException e){
                throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_401_METHOD_NOT_ALLOWED);
            }

            if(path.length() > 2048){
                throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_414_BAD_REQUEST);
            }

            if(!version.matches("HTTP/\\d\\.\\d")){
                throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
            }

            message.setMethod(method);
            message.setPath(path);
            message.setVersion(version);
        }
        catch (IndexOutOfBoundsException e){
            throw new HttpParseException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }
        catch (HttpParseException e){ //Todos os erros http dentro do try são propagados para fora
            throw e;
        }
        catch(Exception e){ // Todos os erros inexperados são categorizados como internal server error
            throw new HttpParseException(HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);
        }
    }

    private void extractHeaders(TreeNode AST) {
        List<Node> headers = AST.getNodeByType("HEADER");

        Map<String, String> headerMap = new HashMap<>();

        for (Node node : headers) {
            TreeNode header = (TreeNode) node;
            String name = header.getNodeByType("HEADER_NAME").isEmpty()
                ? header.getNodeByType("NON_STANDARD_HEADER").get(0).getExpression()
                : header.getNodeByType("HEADER_NAME").get(0).getExpression();

            String value = header.getNodeByType("HEADER_VALUE").get(0).getExpression();
            headerMap.put(name, value);
        }

        message.setHeaders(headerMap);
    }

    private void extractBody(TreeNode AST) {
        List<Node> bodyNodes = AST.getNodeByType("BODY");
        if (!bodyNodes.isEmpty()) {
            TreeNode bodyNode = (TreeNode) bodyNodes.get(0);
            String body = bodyNode.getExpression();
            message.setBody(body);
        }
    }
}

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

    public HttpMessage buildFrom(TreeNode AST) {
        extractRequestLine(AST);
        extractHeaders(AST);
        extractBody(AST);
        return message;
    }

    private void extractRequestLine(TreeNode AST) {
        TreeNode requestLine = (TreeNode) AST.getNodeByType("REQUEST_LINE").get(0);

        String methodStr = requestLine.getNodeByType("METHOD").get(0).getExpression();
        String path = requestLine.getNodeByType("PATH").get(0).getExpression();
        String version = requestLine.getNodeByType("VERSION").get(0).getExpression();

        HttpMethod method; // Assumindo enum HttpMethod

        method = HttpMethod.valueOf(methodStr);

        message.setMethod(method);
        message.setPath(path);
        message.setVersion(version);
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

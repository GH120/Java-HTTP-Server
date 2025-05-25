package com.example.parser;

import com.example.http.HttpMessage;
import com.example.http.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestReader {


    public HttpMessage process(InputStream inputStream){

        var message = new HttpMessage();

        try{

            HttpParser httpParser = new HttpParser();

            httpParser.parse(new HttpLexer().tokenize(readRawHttpHeader(inputStream)));

            TreeNode AbstractSyntaxTree = httpParser.getTree();

            populateRequestLine(AbstractSyntaxTree, message);
            populateHeaders(AbstractSyntaxTree, message);

            //Lê o corpo da mensagem depois de construída com os headers
            //Refatorar depois, lógica separada de criar mensagem e ler corpo confusa
            message.setBody(readRawHttpBody(inputStream, message));

        }
        catch(IOException e){
            e.printStackTrace();
        }

        return message;
    }

    private void populateRequestLine(TreeNode AST, HttpMessage message) {
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

    private void populateHeaders(TreeNode AST, HttpMessage message) {
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

    private void populateBody(TreeNode AST, HttpMessage message) {
        List<Node> bodyNodes = AST.getNodeByType("BODY");
        if (!bodyNodes.isEmpty()) {
            TreeNode bodyNode = (TreeNode) bodyNodes.get(0);
            String body = bodyNode.getExpression();
            message.setBody(body);
        }
    }

    //TODO: está ignorando mensagens com body, adicionar checagem de body (pois para no duplo \n\r dos headers)
    private String readRawHttpHeader(InputStream inputStream) throws IOException{

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            
        int[] EOF   = {13,10,13,10};
        int pointer = 0;

        for(int _byte; (_byte = inputStream.read()) >= 0; ){

            byteBuffer.write(_byte);

            //Detecta \n\r\n\r
            if(EOF[pointer] == _byte) 
                pointer++;
            else 
                pointer = 0;

            if(pointer == 4) break;

        }

        return byteBuffer.toString(StandardCharsets.US_ASCII);
    }

    //Lê separadamente a parte do corpo da mensagem, considerando que o header content length já foi populado
    private String readRawHttpBody(InputStream inputStream, HttpMessage message) throws IOException{

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        String lengthHeader = message.getHeaders().get("Content-Length");

        if(lengthHeader == null) return null;

        int bodyLength = Integer.parseInt(lengthHeader.trim());

        for(int count = 0;  count < bodyLength; count++){

            int _byte = inputStream.read();

            byteBuffer.write(_byte);

        }

        return byteBuffer.toString();
    }
}

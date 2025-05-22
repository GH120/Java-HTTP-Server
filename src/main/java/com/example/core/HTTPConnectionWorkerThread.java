package com.example.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpMessage;
import com.example.http.HttpMethod;
import com.example.parser.HttpBuilder;
import com.example.parser.HttpLexer;
import com.example.parser.HttpParser;
import com.example.parser.TreeNode;

public class HttpConnectionWorkerThread extends Thread{

    private Socket socket;

    HttpConnectionWorkerThread(Socket socket){
        this.socket = socket;
    }

    public void run(){

        InputStream  inputStream  = null;
        OutputStream outputStream = null;
                

        try{
            //Socket retornada que está se comunicando
                //Espera aceitar uma conexão
                System.out.println("Connection accepted" + socket.getInetAddress());
                
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                HttpMessage message = getRequest(inputStream);

                handleRequest(message);
                
                defaultResponse(outputStream);

        }
        catch(Exception e){
            System.out.println("Erro na comunicação");
            e.printStackTrace();
        }
        finally{
            try{
                inputStream.close();
                outputStream.close();
                socket.close();
            }
            catch(Exception e){

            }
        }
    }

    private HttpMessage getRequest(InputStream inputStream){

        HttpMessage message = new HttpMessage();
        HttpBuilder builder = new HttpBuilder();

        try{

            String rawHttpRequest = readHttpRequestRaw(inputStream);

            HttpParser parser = new HttpParser();

            parser.parse(new HttpLexer().tokenize(rawHttpRequest));

            TreeNode AbstractSyntaxTree = parser.getTree();

            message = builder.buildFrom(AbstractSyntaxTree);

        }
        catch(IOException e){
            e.printStackTrace();
        }

        return message;
    }

    private String readHttpRequestRaw(InputStream inputStream) throws IOException{

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

    //Colocar isso na classe HttpMessage?
    private String httpResponseStringFrom(String content){

        final String CRLF = "\n\r";

        return "HTTP/1.1 200 OK" + CRLF + "Content-Length: " + content.getBytes().length + CRLF + CRLF + content + CRLF + CRLF; //Status line: HTTP Version Response_code Response_message;
    }
    
    private void defaultResponse(OutputStream outputStream) throws IOException{
        //O que ele iria retornar depende do requisitado
        String html = "<html><head><title>Simple Java HTTP Server</title></head><body>This is an example</body></html>";
        
        String response = httpResponseStringFrom(html);

        outputStream.write(response.getBytes());
    }

    private void handleRequest(HttpMessage request) throws WebRootNotFoundException{

        Configuration  configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        WebRootHandler handler = new WebRootHandler(configuration.getWebroot());

        switch(request.getMethod()){

            case GET:{

                String path = request.getPath();

                System.out.println(path);

                File file = handler.getFile(path);

                System.out.println(file);

                break;
            }
        }
    }

    private void printFilePaths(String directoryPath){
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        if (files != null){
            for (File file : files){
                if(file.isFile()){
                    System.out.println(file.getAbsolutePath());
                }
            }
        }else{
            System.out.println("vazio");
        }


    }
}

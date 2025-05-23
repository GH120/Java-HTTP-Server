package com.example.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpMessage;
import com.example.http.HttpResponse;
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

                handleRequest(message, outputStream);
                
                // defaultResponse(outputStream);

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

        HttpMessage httpMessage = new HttpMessage();
        HttpBuilder builder = new HttpBuilder();

        try{

            String rawHttpRequest = readHttpRequestRaw(inputStream);

            HttpParser httpParser = new HttpParser();

            httpParser.parse(new HttpLexer().tokenize(rawHttpRequest));

            TreeNode AbstractSyntaxTree = httpParser.getTree();

            httpMessage = builder.buildFrom(AbstractSyntaxTree);

        }
        catch(IOException e){
            e.printStackTrace();
        }

        return httpMessage;
    }

    //TODO: está ignorando mensagens com body, adicionar checagem de body (pois para no duplo \n\r dos headers)
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

    private void handleRequest(HttpMessage request, OutputStream output) throws WebRootNotFoundException, IOException{

        Configuration  configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        WebRootHandler handler = new WebRootHandler(configuration.getWebroot());

        switch(request.getMethod()){

            case GET:{

                String path = request.getPath();

                String contentType = path.endsWith(".png")? "image/png" : null;
                       contentType = path.endsWith(".jpg")? "image/jpg" : null;    

                File file = handler.getFile(path);

                System.out.println("Path do arquivo");
                System.out.println(file);

                byte[] body = Files.readAllBytes(file.toPath());

                System.out.println(body.length);

                var response = HttpResponse.OK(body.length, contentType);

                //Escreve o cabeçário
                output.write(response.toString().getBytes());

                //Escreve o corpo
                output.write(body);
                output.flush();

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

package com.example.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpMessage;
import com.example.http.HttpResponse;

abstract public class HttpRouter {

    abstract public void handleRequest(HttpMessage message, OutputStream output);
}
















class ExampleRouter extends HttpRouter{

    Configuration  configuration;
    WebRootHandler handler;

    public void handleRequest(HttpMessage request, OutputStream output){

        configuration = ConfigurationManager.getInstance().getCurrentConfiguration();
        

        try{

            handler = new WebRootHandler(configuration.getWebroot());

            printFilePaths(configuration.getWebroot());

            switch(request.getMethod()){

                case GET: handleGet(request, output); break;

                default: break;
            }
        }
        catch(Exception e){
            System.out.println(request.getPath());
            e.printStackTrace();
        }
    }

    private void handleGet(HttpMessage request, OutputStream output) throws IOException, WebRootNotFoundException{
        
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

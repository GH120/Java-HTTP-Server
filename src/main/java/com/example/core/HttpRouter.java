package com.example.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

abstract public class HttpRouter {

    abstract public void handleRequest(HttpRequest message, OutputStream output);
}
















class ExampleRouter extends HttpRouter{

    Configuration  configuration;
    WebRootHandler handler;

    public void handleRequest(HttpRequest request, OutputStream output){

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

    private void handleGet(HttpRequest request, OutputStream output) throws IOException, WebRootNotFoundException{
        
        String path = request.getPath();

        String contentType = path.endsWith(".png")? "image/png" : null;
                contentType = path.endsWith(".jpg")? "image/jpg" : null;    

        File file = handler.getFile(path);

        System.out.println("Path do arquivo");
        System.out.println(file);

        byte[] body = Files.readAllBytes(file.toPath());

        System.out.println(body.length);

        HttpStreamWriter.send(HttpResponse.OK(body, contentType), output);
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

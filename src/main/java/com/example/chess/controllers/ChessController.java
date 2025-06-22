package com.example.chess.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.HttpController;
import com.example.core.HttpRouter;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

public class ChessController extends HttpController{

    public ChessController(){
        super(""); 

        addController(new ChessAPI()); //Primeiro tenta passar para a chessAPI
        addController(new StaticHttpServerRouter()); //Se não conseguir, trata a requisição como uma estática
    }
}



class StaticHttpServerRouter implements HttpRouter{

    private Configuration     configuration;
    private WebRootHandler    handler;

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) {

        configuration = ConfigurationManager.getInstance().getCurrentConfiguration();
        
        //Senão, gerencia cada método de requisição (GET, POST...)
        try{

            handler = new WebRootHandler(configuration.getWebroot());

            //Mover isso para um logger
            System.out.println(request.getPath());

            switch(request.getMethod()){
                
                case GET: handleGet(request, output); break;

                default: break;
            }
        }
        catch(Exception e){
            System.out.println("Erro na requisição");
            e.printStackTrace();
        }
    }
    
     /**Retorna arquivos hospedados no webroot do servidor */
    private void handleGet(HttpRequest request, OutputStream output) throws IOException, WebRootNotFoundException{
        
        String path        = request.getPath();
        String contentType = request.getContentType();

        File file = handler.getFile(path);

        System.out.println("Path do arquivo");
        System.out.println(file);

        byte[] body     = Files.readAllBytes(file.toPath());

        HttpStreamWriter.send(HttpResponse.OK(body, contentType), output);
    }

    @Override
    public boolean hasRoute(String path) {
        // TODO Auto-generated method stub
        return true;
    }
}

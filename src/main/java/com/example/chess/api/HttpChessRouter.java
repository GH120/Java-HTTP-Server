package com.example.chess.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;


import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.HttpRouter;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpMessage;
import com.example.http.HttpResponse;

public class HttpChessRouter extends HttpRouter{

    private Configuration     configuration;
    private WebRootHandler    handler;
    private final ChessAPI    API = new ChessAPI();

    public HttpChessRouter(){
        super(); 
    }

    public void handleRequest(HttpMessage request, OutputStream output){

        configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        //Se for uma chamada a API, direciona a ela
        try{
            if(API.hasEndpoint(request.getPath())){

                API.handleRoute(request, output);
            }
        }
        catch(Exception e){
            System.out.println("Erro na chamada a API");
            e.printStackTrace();
        }


        //Senão, gerencia cada método de requisição (GET, POST...)
        try{

            handler = new WebRootHandler(configuration.getWebroot());

            //Mover isso para um logger
            System.out.println("IS API CALL " + API.hasEndpoint(request.getPath()));
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

    private void handleGet(HttpMessage request, OutputStream output) throws IOException, WebRootNotFoundException{
        
        String path        = request.getPath();
        String contentType = getContentType(path);

        File file = handler.getFile(path);

        System.out.println("Path do arquivo");
        System.out.println(file);

        byte[] body     = Files.readAllBytes(file.toPath());
        var    response = HttpResponse.OK(body.length, contentType);

        //Escreve o cabeçário
        output.write(response.toString().getBytes());

        //Escreve o corpo
        output.write(body);
        output.flush();
    }

    

    private String getContentType(String path){

        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".json")) return "application/json";

        return "application/octet-stream";
    }
}

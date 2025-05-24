package com.example.chess.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import com.example.chess.controlers.ChessMatchMaker;
import com.example.chess.models.Player;
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
    private ArrayList<String> apiRoute;

    public HttpChessRouter(){
        super(); createRoutes();
    }

    public void handleRequest(HttpMessage request, OutputStream output){

        configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        boolean IsAPIRequest = apiRoute.contains(request.getPath());

        try{

            handler = new WebRootHandler(configuration.getWebroot());

            System.out.println("IS API CALL " + IsAPIRequest);
            System.out.println(request.getPath());

            if(IsAPIRequest){

                handleRoute(request, output);
            }
            else{
                switch(request.getMethod()){
                    
                    case GET: handleGet(request, output); break;
                }
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

    private void handleRoute(HttpMessage request, OutputStream output) throws Exception{

        System.out.println("API ativada");

        request.print();

        switch(request.getPath()){

            case "/api/findMatch":{

                Player player = Player.fromRequest(request);

                ChessMatchMaker.getInstance().findDuel(player);

                System.out.println(player);

                break;
            }
            case "/api/move":{
                
                break;
            }

            case "/api/state":{

                break;
            }

            case "/api/reset":{

                break;
            }
        }

        var response = HttpResponse.OK(0, null);

        output.write(response.toString().getBytes());
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

    //Guarda uma lista de todos os endpoints usados para ações
    private void createRoutes(){
        apiRoute = new ArrayList<>();

        apiRoute.add("/api/findMatch");
        apiRoute.add("/api/move");
        apiRoute.add("/api/state");
        apiRoute.add("/api/reset");
    }
}

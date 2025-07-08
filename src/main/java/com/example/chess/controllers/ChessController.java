package com.example.chess.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.HttpController;
import com.example.core.HttpRouter;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpMethod;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;
import com.example.core.HttpFilter;

public class ChessController extends HttpController{

    public ChessController(){
        super(""); 

        addController(new OptionsFilter(new ChessAPI())); //Primeiro tenta passar para a chessAPI
        addController(new OptionsFilter(new StaticHttpServerRouter())); //Se não conseguir, trata a requisição como uma estática
    }
}

//Filtro que retorna as opções de retorno do servidor quando o cliente pergunta
class OptionsFilter extends HttpFilter {
    
    public OptionsFilter(HttpRouter router){
        super(router);
    }

    protected boolean intercept(HttpRequest request, InputStream input, OutputStream output) throws IOException{

        System.out.println("Interceptada por Options? " + (request.getMethod() == HttpMethod.OPTIONS));

        if(request.getMethod() == HttpMethod.OPTIONS){

            System.out.println("Requisição interceptada com options");

            var headers = new HashMap<String, String>();
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type");
            headers.put("Access-Control-Max-Age", "86400"); // cache de 1 dia
            headers.put("Content-Length", "0");

            HttpResponse response = new HttpResponse()
                .setStatusCode(204)
                .setVersion("HTTP/1.1")
                .setHeaders(headers)
                .setBody(new byte[0]); // corpo vazio

            HttpStreamWriter.send(response, output);

            return true;

        }

        return false;
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

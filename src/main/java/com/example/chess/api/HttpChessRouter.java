package com.example.chess.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import com.example.chess.controlers.ChessMatch.ChessError;
import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.HttpRouter;
import com.example.core.io.WebRootHandler;
import com.example.core.io.WebRootNotFoundException;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

public class HttpChessRouter extends HttpRouter{

    private Configuration     configuration;
    private WebRootHandler    handler;
    private final ChessAPI    API = new ChessAPI();

    public HttpChessRouter(){
        super(); 
    }

    /**CRUD básico para requisições comuns, requisições de API redirecionadas a ChessAPI*/
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output){

        configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        //Se for uma chamada a API, direciona a ela
        try{
            if(API.hasEndpoint(request.getPath())){

                API.handleRoute(request, input, output);

                return;
            }

        }
        catch(ChessError e){ //Mover esse handler de erros pra dentro da API

            try{
                HttpStreamWriter.send(HttpResponse.BAD_REQUEST(e.getLocalizedMessage().getBytes(), null), output);
            }
            catch(IOException io){
                System.out.println("Envio de erro não funcionou");
                io.printStackTrace();
            }


            e.printStackTrace();
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
}

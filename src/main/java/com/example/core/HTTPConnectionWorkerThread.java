package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.example.chess.api.HttpChessRouter;
import com.example.http.HttpRequest;
import com.example.parser.HttpStreamReader;

public class HttpConnectionWorkerThread extends Thread{

    private Socket     socket;
    private HttpRouter router = new HttpChessRouter();

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
            
            inputStream  = socket.getInputStream();
            outputStream = socket.getOutputStream();

            //Roda 100 loops de esperar request até parar 
                
            //Cria uma mensagem http a partir do fluxo de dados de input
            HttpRequest message = new HttpStreamReader().processRequest(inputStream);
            
            //Manda mensagem para o router decidir o que fazer com ela
            //Atualmente ele pode só interpretá-la literalmente (como get arquivo) ou direcioná-la para a api baseado em seu endpoint
            
            router.handleRequest(message, inputStream, outputStream);
            
        }
        catch(Exception e){
            System.out.println("Erro na comunicação");
            e.printStackTrace();
        }
        finally{

            System.out.println("Terminou " + socket.getInetAddress());

            try{
                inputStream.close();
                outputStream.close();
                socket.close();
            }
            catch(Exception e){

            }
        }
    }

    public void setRouter(HttpRouter router){
        this.router = router;
    }
}
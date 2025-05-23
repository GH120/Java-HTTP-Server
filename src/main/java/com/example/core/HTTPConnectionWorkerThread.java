package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.example.http.HttpMessage;
import com.example.parser.HttpBuilder;

public class HttpConnectionWorkerThread extends Thread{

    private Socket     socket;
    private HttpRouter router = new ExampleRouter();

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

            HttpMessage message = new HttpBuilder().getRequest(inputStream);

            router.handleRequest(message, outputStream);
            
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

    public void setRouter(HttpRouter router){
        this.router = router;
    }
}
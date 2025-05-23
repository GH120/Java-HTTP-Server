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
            
            inputStream = socket.getInputStream();
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
}
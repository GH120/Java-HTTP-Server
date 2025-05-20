package com.example.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerListenerThread extends Thread{

    private int port;
    private String webroot;
    private ServerSocket serverSocket;

    public ServerListenerThread(int port, String webroot){
        this.port    = port;
        this.webroot = webroot;

        try{
            this.serverSocket = new ServerSocket(port);
        }
        catch(IOException exception){
            exception.printStackTrace();
        }
    }

    public void run(){

        try{
            while(serverSocket.isBound() && !serverSocket.isClosed()){
    
                //Aceita uma conexão socket, roda a thread dela e concorrentemente espera a proxima
                Socket socket = serverSocket.accept();
    
                //Processa a requisição paralelamente
                HTTPConnectionWorkerThread worker = new HTTPConnectionWorkerThread(socket);
    
                worker.start();
            }   

        }catch(IOException e){
            e.printStackTrace();
        }
        finally{
            if(serverSocket == null) return;

            try{
                serverSocket.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}

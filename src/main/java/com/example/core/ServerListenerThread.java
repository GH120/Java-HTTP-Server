package com.example.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.config.Configuration;

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

        while(serverSocket.isBound() && !serverSocket.isClosed()){

            ServerListenerWorker worker = new ServerListenerWorker(serverSocket);

            worker.start();
        }   
    }
}

package com.example.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


//Ideia: inversão de dependência: fazer essa classe chamar a interface que roda o xadrez dentro dela
//Isso seria geral o suficiente para mudar a execução para qualquer outro programa rodando em várias conexões http
//Talvez armazenar o programa a ser rodado no ConfigurationManager?
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

                System.out.println("ACEITOU CONEXÃO");

    
                //Processa a requisição paralelamente
                HttpConnectionWorkerThread worker = new HttpConnectionWorkerThread(socket);
    
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

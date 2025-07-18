package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.example.chess.controllers.ChessController;
import com.example.http.HttpRequest;
import com.example.http.HttpStreamReader;

//Ideia: inversão de dependência: fazer essa classe chamar a interface que roda o xadrez dentro dela
//Isso seria geral o suficiente para mudar a execução para qualquer outro programa rodando em várias conexões http
//Talvez armazenar o programa a ser rodado no ConfigurationManager?
public class HttpConnectionWorkerThread extends Thread{

    private Socket     socket;
    private HttpRouter router = new ChessController(); //Fazer ele carregar esse router das configurações, ou talvez passar como argumento

    HttpConnectionWorkerThread(Socket socket){
        System.out.println("CRIOU NOVO WORKER THREAD");
        this.socket = socket;
    }

    //Limitar acessos a findMatch a depender do número de jogadores já ativos e a capacidade do servidor
    public void run(){

        InputStream  inputStream  = null;
        OutputStream outputStream = null;
                

        try{
            //Socket retornada que está se comunicando
            //Espera aceitar uma conexão
            System.out.println("Connection accepted" + socket.getInetAddress());
            
            inputStream  = socket.getInputStream();
            outputStream = socket.getOutputStream();

                

            //Cria uma mensagem http a partir do fluxo de dados de input
            HttpRequest message = new HttpStreamReader().processRequest(inputStream);
                
            //Manda mensagem para o router decidir o que fazer com ela
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
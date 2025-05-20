package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenerWorker extends Thread{

    private ServerSocket serverSocket;

    ServerListenerWorker(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void run(){
        try{
            //Socket retornada que está se comunicando
                //Espera aceitar uma conexão
                Socket socket = serverSocket.accept();
                
                System.out.println("Connection accepted" + socket.getInetAddress());
                
                InputStream  inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                
                String html = "<html><head><title>Simple Java HTTP Server</title></head><body>This is an example</body></html>";
                
                final String CRLF = "\n\r";
                
                String response = "HTTP/1.1 200 OK" + CRLF + "Content-Length: " + html.getBytes().length + CRLF + CRLF + html + CRLF + CRLF; //Status line: HTTP Version Response_code Response_message;

                outputStream.write(response.getBytes());
                
                inputStream.close();
                outputStream.close();
                socket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

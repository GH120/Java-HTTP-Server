package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPConnectionWorkerThread extends Thread{

    private Socket socket;

    HTTPConnectionWorkerThread(Socket socket){
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
                
                String html = "<html><head><title>Simple Java HTTP Server</title></head><body>This is an example</body></html>";
                
                final String CRLF = "\n\r";
                
                String response = "HTTP/1.1 200 OK" + CRLF + "Content-Length: " + html.getBytes().length + CRLF + CRLF + html + CRLF + CRLF; //Status line: HTTP Version Response_code Response_message;

                outputStream.write(response.getBytes());

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

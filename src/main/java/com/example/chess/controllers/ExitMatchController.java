package com.example.chess.controllers;

import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.Player;
import com.example.chess.services.ChessMatch;
import com.example.chess.services.ChessMatchManager;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

public class ExitMatchController extends HttpController{

    public ExitMatchController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws Exception {
        
        Player player = Player.fromRequest(request);
        
        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
        match.quit();

        //Tem que avisar o adversário
        //match.semaphor.notifyMove();

        HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);

    }
}

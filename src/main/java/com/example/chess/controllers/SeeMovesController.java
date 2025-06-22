package com.example.chess.controllers;

import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.services.ChessMatch;
import com.example.chess.services.ChessMatchManager;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

public class SeeMovesController extends HttpController{

    public SeeMovesController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws Exception {

        Player player = Player.fromRequest(request);

        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
        Position position = Position.fromRequest(request);

        match.showPossibleMoves(position);

        HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);
    }

}

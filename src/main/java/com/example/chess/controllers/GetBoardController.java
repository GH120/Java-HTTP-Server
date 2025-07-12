package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Piece;
import com.example.chess.models.Player;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStreamWriter;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonParseException;

public class GetBoardController extends HttpController{

    public GetBoardController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound {
        
        Player player = Player.fromRequest(request);

        try{
            ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
            
            Piece[][] board = match.getChessModel().getBoard();
            
            HttpStreamWriter.send(HttpResponse.OK(Json.from(board), "application/json"), output);
        }
        catch(MatchNotFound e){
            HttpStreamWriter.send(HttpResponse.BAD_REQUEST(e.getLocalizedMessage().getBytes(), null), output);
        }
            
    }

}

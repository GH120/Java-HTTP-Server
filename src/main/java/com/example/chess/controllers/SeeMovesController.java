package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStreamWriter;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonParseException;

public class SeeMovesController extends HttpController{

    public SeeMovesController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound  {

        Player player = Player.fromRequest(request);

        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
        Position position = Position.fromRequest(request);

        List<Move> moves = match.getAllPossibleMoves(position);

        byte[] body = Json.from(moves.toArray());

        System.out.println(body.length);

        HttpStreamWriter.send(
            HttpResponse.OK(
                body,
                "application/json"
            ), 
            output
        );
    }

}

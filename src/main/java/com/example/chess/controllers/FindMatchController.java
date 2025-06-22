package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.Player;
import com.example.chess.services.ChessMatch;
import com.example.chess.services.ChessMatchMaker;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.json.Json;
import com.example.parser.HttpStreamWriter;
import com.fasterxml.jackson.core.JsonParseException;

public class FindMatchController extends HttpController{

    public FindMatchController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }


    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound{

        Player player = Player.fromRequest(request);

        //Método assíncrono que espera outro usuário aceitar um duelo
        ChessMatchMaker.getInstance().findDuel(player);

        //Uma vez passada a parte de espera, então encontrou uma partida
        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

        HttpStreamWriter.send(HttpResponse.OK(Json.from(match.getOpponent(player)),null), output);
    }

}

package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Player;
import com.example.chess.models.Turn;
import com.example.chess.services.ChessMatchMaker;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStreamWriter;
import com.example.json.Json;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

public class FindMatchController extends HttpController{

    public FindMatchController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }


    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound{

        //Fazer parte de conseguir player logado depois, vamos tratar apenas guest agora
        // Player player = Player.fromRequest(request);

        //Cria um guest
        Player player = ChessMatchManager.getInstance().createGuest();

        //Método assíncrono que espera outro usuário aceitar um duelo
        ChessMatchMaker.getInstance().findDuel(player);

        //Uma vez passada a parte de espera, então encontrou uma partida
        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

        //Cria o DTO da partida encontrada junto com o jogador selecionado
        MatchFound matchStart = new MatchFound(match.history.lastTurn(), player);

        HttpStreamWriter.send(HttpResponse.OK(Json.from(matchStart),"application/json"), output);
    }

    private record MatchFound(
        @JsonProperty("turn")    
        Turn turn, 
        
        @JsonProperty("player")
        Player player
    ){

    }

}
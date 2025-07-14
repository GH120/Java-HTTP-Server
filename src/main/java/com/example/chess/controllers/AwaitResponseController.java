package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Piece;
import com.example.chess.models.Player;
import com.example.chess.models.Turn;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStreamWriter;
import com.example.json.Json;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

public class AwaitResponseController extends HttpController{

    public AwaitResponseController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, InterruptedException, MatchNotFound{
        
        Player player = Player.fromRequest(request);

        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

        System.out.println("player " + player.name + "is waiting");
        
        match.semaphor.waitForMove(player);

        System.out.println("player " + player.name + "now can play");

        //Tem que tratar o caso que o jogo terminou ou que o adversário quitou

        Turn turn = match.history.lastTurn();

        HttpResponse response = HttpResponse.OK(Json.from(turn), "application/json");

        HttpStreamWriter.send(response, output);

        //Melhor implementação: 
        //Tornar o MatchWatcher em um observador sincronizado, que controla o acesso de ambos os jogadores
        //Quando um estiver esperando, ele fica num estado de espera, e quando ele for acionado pelo outro jogador automaticamente dispara
        //Dessa forma, um jogador não precisa nem saber a existência do outro jogador, apenas o seu watcher, que pode ser mapeado no ChessMatchManager
        //Ele irá guardar o estado da última jogada e todos os efeitos a ela associados, tendo uma função para retornar os eventos da jogada (se foi cheque, se teve en passant...)
        //Usar essa função que retorna um json para montar uma requisição http e mandar de volta para o adversário e para o jogador.
    }
}

record TurnState (
    @JsonProperty("turn")
    Turn turn
){

}
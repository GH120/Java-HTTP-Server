package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Player;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;
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
        
        match.semaphor.waitForMove();

        //Tem que tratar o caso que o jogo terminou ou que o adversário quitou
        Move move = match.getChessModel().getLastMove();

        HttpStreamWriter.send(HttpResponse.OK(move.toJson().getBytes(), "application/json"), output);

        //Melhor implementação: 
        //Tornar o MatchWatcher em um observador sincronizado, que controla o acesso de ambos os jogadores
        //Quando um estiver esperando, ele fica num estado de espera, e quando ele for acionado pelo outro jogador automaticamente dispara
        //Dessa forma, um jogador não precisa nem saber a existência do outro jogador, apenas o seu watcher, que pode ser mapeado no ChessMatchManager
        //Ele irá guardar o estado da última jogada e todos os efeitos a ela associados, tendo uma função para retornar os eventos da jogada (se foi cheque, se teve en passant...)
        //Usar essa função que retorna um json para montar uma requisição http e mandar de volta para o adversário e para o jogador.
    }
}

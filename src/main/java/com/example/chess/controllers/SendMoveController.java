package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.Move;
import com.example.chess.models.Player;
import com.example.chess.services.ChessMatch;
import com.example.chess.services.ChessMatch.ChessError;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;
import com.fasterxml.jackson.core.JsonParseException;

public class SendMoveController extends HttpController{


    public SendMoveController(String endpoint) {
        super(endpoint);
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound, ChessError {

        Player player = Player.fromRequest(request);

        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

        //Depois criar um move intent que é processado em uma move usando o estado da partida (para movimentos como pawn to e4)
        // Alternativa: MoveIntent interpreta movimentos como "e4" ou "Nf3" usando o estado atual.  
        // Prós: notação natural, resolve ambiguidades. Contras: complexidade extra.  
        // Se necessário: MoveIntent intent = MoveIntent.fromRequest(request); Move move = intent.toMove(match.getBoard())
        Move move = Move.fromRequest(request);

        //Adicionar um try catch
        match.playMove(player, move); //Irá ativar observers, dentre eles o watcher da partida, que irá avisar o outro jogador

        //Adicionar um condicional, pois se houver uma promoção o oponente não será liberado
        match.semaphor.notifyMove();

        //Adicionar resposta padrão sendo um DTO turnSummary que contém o estado do jogo, da jogada e ,se houver promoção, a promoção escolhida. Pensando em retornar tabuleiro como padrão também
        //Será que é bom criar um json mapper no estilo de um visitor? Talvez seja muito complicado e não sei se haveria justificativa
        HttpStreamWriter.send(HttpResponse.OK(move.toJson().getBytes(), "application/json"), output);
    }

   
}

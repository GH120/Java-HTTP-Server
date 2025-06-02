package com.example.chess.api;

import com.example.chess.controlers.MatchObserver;
import com.example.chess.models.Move;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Position;
import com.example.http.HttpRequest;
import com.example.http.HttpMethod;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchWatcher implements MatchObserver {

    // private final PieceColor       playerColor;
    private final OutputStream     clientOutput;
    private final ExecutorService  executor;
    private final HttpStreamWriter httpWriter;

    public MatchWatcher(PieceColor playerColor, OutputStream clientOutput){
        this.playerColor  = playerColor;
        this.clientOutput = clientOutput;
        this.executor     = Executors.newSingleThreadExecutor(); //Usar ele para não bloquear execução do jogo principal
        this.httpWriter   = new HttpStreamWriter();
    }

    @Override
    public void onMoveExecuted(Move move, PlayerColor player){

        executor.execute(() -> {
            try{
                String eventJson = String.format(
                    "{\\\"event\\\":\\\"move\\\",\\\"from\\\":\\\"%s\\\",\\\"to\\\":\\\"%s\\\",\\\"player\\\":\\\"%s\\\"}",
                    move.origin.toString(),
                    move.destination.toString(),
                    player.toString()
                )

                sendEventToClient(eventJson);
            }
            catch(Exception e){
                System.err.println("Error sending move notification: " + e.getMessage());
            }
        })
    }
}
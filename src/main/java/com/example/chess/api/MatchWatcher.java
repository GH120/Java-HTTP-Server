package com.example.chess.api;

import com.example.chess.controlers.ChessMatch.GameState;
import com.example.chess.controlers.MatchObserver;
import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchWatcher implements MatchObserver {

    // private final PieceColor  playerColor;
    private final OutputStream     clientOutput;
    private final ExecutorService  executor;
    private final HttpStreamWriter httpWriter;

    public MatchWatcher(PlayerColor playerColor, OutputStream clientOutput){
        this.clientOutput = clientOutput;
        this.executor     = Executors.newSingleThreadExecutor(); //Usar ele para não bloquear execução do jogo principal
        this.httpWriter   = new HttpStreamWriter();
    }

    @Override
    public void onMoveExecuted(Move move, PlayerColor player){

        System.out.println("Player ".concat(player.toString()).concat(" played").concat(move.toString()));

        executor.execute(() -> {
            try{
                String eventJson = String.format(
                    "{\"event\":\"move\",\"from\":\"%s\",\"to\":\"%s\",\"player\":\"%s\"}",
                    move.origin.toString(),
                    move.destination.toString(),
                    player.toString()
                );

                System.out.println(eventJson);

                sendEventToClient(eventJson);
            }
            catch(Exception e){
                System.err.println("Error sending move notification: " + e.getMessage());
            }
        });
    }

     @Override
    public void onGameStateChanged(GameState newState) {
        executor.execute(() -> {
            try {
                String eventJson = String.format(
                    "{\"event\":\"gameState\",\"state\":\"%s\"}",
                    newState.toString()
                );
                
                sendEventToClient(eventJson);
            } catch (Exception e) {
                System.err.println("Error sending game state notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onPromotionRequired(Position pawnPosition) {
        // Só notifica se for promoção do próprio jogador
        executor.execute(() -> {
            try {
                String eventJson = String.format(
                    "{\"event\":\"promotionRequired\",\"position\":\"%s\"}",
                    pawnPosition.toString()
                );
                
                sendEventToClient(eventJson);
            } catch (Exception e) {
                System.err.println("Error sending promotion notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onShowPossibleMoves(List<Move> moves) {
        executor.execute(() -> {
            try {
                StringBuilder movesJson = new StringBuilder("[");
                for (Move move : moves) {
                    movesJson.append(String.format(
                        "{\"from\":\"%s\",\"to\":\"%s\"},",
                        move.origin.toString(),
                        move.destination.toString()
                    ));
                }
                if (!moves.isEmpty()) {
                    movesJson.deleteCharAt(movesJson.length() - 1);
                }
                movesJson.append("]");

                String eventJson = String.format(
                    "{\"event\":\"possibleMoves\",\"moves\":%s}",
                    movesJson.toString()
                );
                
                sendEventToClient(eventJson);
            } catch (Exception e) {
                System.err.println("Error sending possible moves notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(String message) {
        executor.execute(() -> {
            try {
                String eventJson = String.format(
                    "{\"event\":\"error\",\"message\":\"%s\"}",
                    message
                );
                
                sendEventToClient(eventJson);
            } catch (Exception e) {
                System.err.println("Error sending error notification: " + e.getMessage());
            }
        });
    }


    private void sendEventToClient(String eventJson) throws IOException {
        HttpResponse response = HttpResponse.OK(eventJson.getBytes("UTF-8"), "application/json");

        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Connection", "keep-alive");

        synchronized (clientOutput){
            HttpStreamWriter.send(response, clientOutput);
        }
    }
}
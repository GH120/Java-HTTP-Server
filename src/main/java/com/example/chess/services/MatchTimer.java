package com.example.chess.services;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Player;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.models.ChessMatch.GameState;

public class MatchTimer implements MatchObserver{

    private final ChessMatch match;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?>       countDownTask;

    public MatchTimer(ChessMatch match){
        this.match    = match;
        this.executor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void onMoveExecuted(Move move, PlayerColor playerColor) {

        Player currentPlayer = match.getPlayer(playerColor);

        System.out.println("Jogou");

        // Pausa o timer do jogador anterior e inicia o do próximo
        if(countDownTask != null) {
            countDownTask.cancel(true);
        }

        countDownTask = executor.scheduleAtFixedRate(() -> {

            Integer time = match.getTime(currentPlayer);

            match.updateTime(currentPlayer, time - 1);

            match.checkTimeOut();

            System.out.println("Jogador " + currentPlayer.name);
            System.out.println("Tempo: " + (time-1));


        }, 1, 1, TimeUnit.SECONDS);

        System.out.println(countDownTask);
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        
        if(hasEnded(newState)) executor.shutdownNow();
    }

    @Override
    public void onPromotionRequired(Position pawnPosition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPromotionRequired'");
    }

    @Override
    public void onShowPossibleMoves(List<Move> moves) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onShowPossibleMoves'");
    }

    @Override
    public void onError(String message) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    public ScheduledExecutorService getExecutor (){
        return executor;
    }

    private boolean hasEnded(GameState state){

        switch(state){
            case DRAW, TIMEOUT, CHECKMATE, EXITED -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

}

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

public class MatchWatcher implements MatchObserver{

    private Move    lastMove;
    private boolean moveReceived = false;

    public synchronized void waitForMove() throws InterruptedException {
        while (!moveReceived) {
            wait(); // Libera o lock até receber notificação
        }
        moveReceived = false; // Reseta para próxima jogada
    }

    public synchronized void notifyMove() {
        moveReceived = true;
        notifyAll(); // Libera as threads bloqueadas
    }

    @Override
    //Deverá contribuir para um objeto ChessMoveSummary que guardará todas as informações da jogada
    //Seria melhor o notificador só ter um método de envio com esse CMS como argumento?
    public void onMoveExecuted(Move move, PlayerColor currentPlayer) {

        lastMove = move;

        notifyMove();
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onGameStateChanged'");
    }

    @Override
    public void onPromotionRequired(Position pawnPosition) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onPromotionRequired'");
    }

    @Override
    public void onShowPossibleMoves(List<Move> moves) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onShowPossibleMoves'");
    }

    @Override
    public void onError(String message) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    public Move getLastMove(){
        return this.lastMove;
    }
}

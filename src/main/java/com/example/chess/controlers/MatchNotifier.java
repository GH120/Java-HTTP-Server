package com.example.chess.controlers;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.controlers.ChessMatch.GameState;
import com.example.chess.models.Move;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Position;


public class MatchNotifier {
    private final List<MatchObserver> observers = new ArrayList<>();

    public void addObserver(MatchObserver observer) {
        if (observer != null) observers.add(observer);
    }

    public void removeObserver(MatchObserver observer) {
        observers.remove(observer);
    }

    public void notifyMove(Move move, PieceColor currentPlayer) {
        for (MatchObserver obs : observers) {
            obs.onMoveExecuted(move, currentPlayer);
        }
    }

    public void notifyPromotionRequired(Position pawnPos) {
        for (MatchObserver obs : observers) {
            obs.onPromotionRequired(pawnPos);
        }
    }

    public void notifyStateChange(GameState state) {
        for (MatchObserver obs : observers) {
            obs.onGameStateChanged(state);
        }
    }

    public void notifyError(String error) {
        for (MatchObserver obs : observers) {
            obs.onError(error);
        }
    }
}

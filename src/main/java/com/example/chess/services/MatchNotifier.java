package com.example.chess.services;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.models.ChessMatch.GameState;


public class MatchNotifier {
    private final List<MatchObserver> observers = new ArrayList<>();

    public void addObserver(MatchObserver observer) {
        if (observer != null) observers.add(observer);
    }

    public void removeObserver(MatchObserver observer) {
        observers.remove(observer);
    }

    public void notifyMove(Move move, PlayerColor currentPlayer) {
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

    public void notifyPossibleMoves(List<Move> moves){
        for (MatchObserver obs : observers) {
            obs.onShowPossibleMoves(moves);
        }
    }

    public void notifyError(String error) {
        for (MatchObserver obs : observers) {
            obs.onError(error);
        }
    }
}

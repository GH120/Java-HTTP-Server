package com.example.chess.services;

import java.util.List;

import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.services.ChessMatch.GameState;

public interface MatchObserver {

    void onMoveExecuted(Move move, PlayerColor currentPlayer);
    void onGameStateChanged(GameState newState);
    void onPromotionRequired(Position pawnPosition);
    void onShowPossibleMoves(List<Move> moves);
    void onError(String message);
}

package com.example.chess.controlers;

import com.example.chess.controlers.ChessMatch.GameState;
import com.example.chess.models.Move;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Position;

public interface MatchObserver {

    void onMoveExecuted(Move move, PieceColor currentPlayer);
    void onGameStateChanged(GameState newState);
    void onPromotionRequired(Position pawnPosition);
    void onError(String message);
}

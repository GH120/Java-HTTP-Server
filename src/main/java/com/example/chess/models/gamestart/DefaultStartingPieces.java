package com.example.chess.models.gamestart;

import com.example.chess.models.ChessBoard;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.models.StartingPieces;
import com.example.chess.models.chesspieces.*;

public class DefaultStartingPieces implements StartingPieces{
    
    public void populateBoard(ChessBoard model) {
        model.placePiece(new Rook(new Position(0, 0), PlayerColor.BLACK));       // Torre a1
        model.placePiece(new Knight(new Position(1, 0), PlayerColor.BLACK));     // Cavalo b1
        model.placePiece(new Bishop(new Position(2, 0), PlayerColor.BLACK));     // Bispo c1
        model.placePiece(new Queen(new Position(3, 0), PlayerColor.BLACK));      // Rainha d1
        model.placePiece(new King(new Position(4, 0), PlayerColor.BLACK));       // Rei e1
        model.placePiece(new Bishop(new Position(5, 0), PlayerColor.BLACK));     // Bispo f1
        model.placePiece(new Knight(new Position(6, 0), PlayerColor.BLACK));     // Cavalo g1
        model.placePiece(new Rook(new Position(7, 0), PlayerColor.BLACK));       // Torre h1

        // Peões brancos (linha 1)
        for (int col = 0; col < 8; col++) {
            model.placePiece(new Pawn(new Position(col, 1), PlayerColor.BLACK)); // a2-h2
        }

        // Peças pretas (linha 7 - peças principais)
        model.placePiece(new Rook(new Position(0, 7), PlayerColor.WHITE));       // Torre a8
        model.placePiece(new Knight(new Position(1, 7), PlayerColor.WHITE));     // Cavalo b8
        model.placePiece(new Bishop(new Position(2, 7), PlayerColor.WHITE));     // Bispo c8
        model.placePiece(new Queen(new Position(3, 7), PlayerColor.WHITE));      // Rainha d8
        model.placePiece(new King(new Position(4, 7), PlayerColor.WHITE));       // Rei e8
        model.placePiece(new Bishop(new Position(5, 7), PlayerColor.WHITE));     // Bispo f8
        model.placePiece(new Knight(new Position(6, 7), PlayerColor.WHITE));     // Cavalo g8
        model.placePiece(new Rook(new Position(7, 7), PlayerColor.WHITE));       // Torre h8

        // Peões pretos (linha 6)
        for (int col = 0; col < 8; col++) {
            model.placePiece(new Pawn(new Position(col, 6), PlayerColor.WHITE)); // a7-h7
        }
    }
}

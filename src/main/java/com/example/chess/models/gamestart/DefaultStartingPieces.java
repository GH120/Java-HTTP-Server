package com.example.chess.models.gamestart;

import com.example.chess.models.ChessModel;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.models.StartingPieces;
import com.example.chess.models.chesspieces.*;

public class DefaultStartingPieces implements StartingPieces{
    
    public void populateBoard(ChessModel model) {
        model.insertPiece(new Rook(new Position(0, 0), PlayerColor.WHITE));       // Torre a1
        model.insertPiece(new Knight(new Position(1, 0), PlayerColor.WHITE));     // Cavalo b1
        model.insertPiece(new Bishop(new Position(2, 0), PlayerColor.WHITE));     // Bispo c1
        model.insertPiece(new Queen(new Position(3, 0), PlayerColor.WHITE));      // Rainha d1
        model.insertPiece(new King(new Position(4, 0), PlayerColor.WHITE));       // Rei e1
        model.insertPiece(new Bishop(new Position(5, 0), PlayerColor.WHITE));     // Bispo f1
        model.insertPiece(new Knight(new Position(6, 0), PlayerColor.WHITE));     // Cavalo g1
        model.insertPiece(new Rook(new Position(7, 0), PlayerColor.WHITE));       // Torre h1

        // Peões brancos (linha 1)
        for (int col = 0; col < 8; col++) {
            model.insertPiece(new Pawn(new Position(col, 1), PlayerColor.WHITE)); // a2-h2
        }

        // Peças pretas (linha 7 - peças principais)
        model.insertPiece(new Rook(new Position(0, 7), PlayerColor.BLACK));       // Torre a8
        model.insertPiece(new Knight(new Position(1, 7), PlayerColor.BLACK));     // Cavalo b8
        model.insertPiece(new Bishop(new Position(2, 7), PlayerColor.BLACK));     // Bispo c8
        model.insertPiece(new Queen(new Position(3, 7), PlayerColor.BLACK));      // Rainha d8
        model.insertPiece(new King(new Position(4, 7), PlayerColor.BLACK));       // Rei e8
        model.insertPiece(new Bishop(new Position(5, 7), PlayerColor.BLACK));     // Bispo f8
        model.insertPiece(new Knight(new Position(6, 7), PlayerColor.BLACK));     // Cavalo g8
        model.insertPiece(new Rook(new Position(7, 7), PlayerColor.BLACK));       // Torre h8

        // Peões pretos (linha 6)
        for (int col = 0; col < 8; col++) {
            model.insertPiece(new Pawn(new Position(col, 6), PlayerColor.BLACK)); // a7-h7
        }
    }
}

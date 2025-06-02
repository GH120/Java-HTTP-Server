package com.example.chess.models;

import com.example.chess.models.chesspieces.Bishop;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Knight;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;
import com.example.chess.models.chesspieces.Rook;

/**Contém os tipos padrões de inicialização do tabuleiro */
public interface StartingPieces {
    // Método abstrato que cada valor do enum deve implementar
    public void populateBoard(ChessModel model);
}
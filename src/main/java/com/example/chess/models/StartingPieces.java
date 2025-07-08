package com.example.chess.models;

/**Contém os tipos padrões de inicialização do tabuleiro */
public interface StartingPieces {
    // Método abstrato que cada valor do enum deve implementar
    public void populateBoard(ChessModel model);
}
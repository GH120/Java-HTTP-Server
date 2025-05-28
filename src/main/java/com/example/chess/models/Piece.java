package com.example.chess.models;

import java.util.List;

abstract public class Piece {
    public Position    position;
    PieceColor  color;

    abstract public List<Move> allowedMoves(Piece[][] board);
    // abstract public List<Move> allowedMovesFrom(Piece[][] board, Position position); 
    //Considerar caso onde moveu, mas a peça continua no tabuleiro
    //Por exemplo, se for uma jogada fantasma, pode ser que a peça que se moveu continua na mesma posição no tabuleiro
    //Solução: ignorar completamente se peça == essa peça


    public PieceColor getColor(){
        return color;
    }

    public boolean enemyPiece(Piece piece){

        return piece != null && piece.getColor() != color;
    }
}

package com.example.chess.models;

import java.util.List;

abstract public class Piece {

    public  Position    position;
    public  PieceColor  color;

    abstract public List<Move>     allowedMoves(Piece[][] board);

    public PieceColor getColor(){
        return color;
    }

    public boolean enemyPiece(Piece piece){

        return piece != null && piece.getColor() != color;
    }

    public void apply(Piece[][] board, Move move){

        // Ao sair da posição, coloca ela como nula no tabuleiro
        board[position.x][position.y] = null; 
        
        // Ao mover para posição, sobrescreve o quadrado atacado/movido
        board[move.destination.x][move.destination.y] = this; 

        
        position = move.destination;
    }
}

class MoveDoesNotOriginateFromPosition extends Exception{

}
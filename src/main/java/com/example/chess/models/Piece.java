package com.example.chess.models;

import java.util.List;

abstract public class Piece {

    public  Position    position;
    public  PieceColor  color;

    abstract public List<Move>     allowedMoves(Piece[][] board);

    //Util
    public PieceColor getColor(){
        return color;
    }

    public boolean enemyPiece(Piece piece){

        return piece != null && piece.getColor() != color;
    }


    /**Atualiza posição da peça interna e no tabuleiro baseado na jogada */
    public void apply(Piece[][] board, Move move){

        // Ao sair da posição, coloca ela como nula no tabuleiro
        board[position.x][position.y] = null; 
        
        // Ao mover para posição, sobrescreve o quadrado atacado/movido
        board[move.destination.x][move.destination.y] = this; 

        
        position = move.destination;
    }

    private void treatSideEffects(Piece[][] board, Move move){

        switch(move.event){
            case EN_PASSANT -> {
                
                Piece victim = move.event.target;

                board[victim.position.x][victim.position.y] = null;
            }
            default -> {

            }
        }
    }
}

class MoveDoesNotOriginateFromPosition extends Exception{

}
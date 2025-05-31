package com.example.chess.models;

import java.util.List;

abstract public class Piece {

    public  Position    position;
    public  PieceColor  color;

    abstract public List<Move>  defaultMoves(Piece[][] board);

    public Piece(Position position, PieceColor color){
        this.position = position;
        this.color = color;
    }

    //Util
    public PieceColor getColor(){
        return color;
    }

    public boolean enemyPiece(Piece piece){

        return piece != null && piece.getColor() != color;
    }

}

class MoveDoesNotOriginateFromPosition extends Exception{

}
package com.example.chess.models;

import java.util.List;

abstract public class Piece {

    public  Position    position;
    public  PlayerColor  color;

    abstract public List<Move>  defaultMoves(Piece[][] board);

    public Piece(Position position, PlayerColor color){
        this.position = position;
        this.color = color;
    }

    //Util
    public PlayerColor getColor(){
        return color;
    }

    public boolean enemyPiece(Piece piece){

        return piece != null && piece.getColor() != color;
    }

    @Override
    public String toString() {
        return "Position: " + position.toString() + " Player:" + color.toString();
    }

}

class MoveDoesNotOriginateFromPosition extends Exception{

}
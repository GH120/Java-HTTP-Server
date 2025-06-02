package com.example.chess.models;

public enum Direction {

    NORTH(0,1), NORTHWEST(-1,1), NORTHEAST(1,1),
    SOUTH(0,-1), SOUTHWEST(-1,-1), SOUTHEAST(1,-1),
    WEST(-1,0), EAST(1,0);

    public final int x;
    public final int y;

    Direction(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Direction rotate90Degrees(boolean clockwise){
        
        int perpendicularX = clockwise? y  : -y;
        int perpendicularY = clockwise? -x :  x;

        for(Direction direcao : values()){
            
            if(direcao.x == perpendicularX){
                if(direcao.y == perpendicularY){
                    return direcao;
                }
            }

        }

        throw new IllegalStateException("Sem direção perpendicular");
    }

    //Curiosidade, quais são as condições para jogar excessão sem precisar declarar?
    public Direction invert(){

        for(Direction direcao : values()){
            if(direcao.x == -x){
                if(direcao.y == -y){
                    return direcao;
                }
            }
        }

        throw new IllegalStateException("Sem direção inversa");
    }
}
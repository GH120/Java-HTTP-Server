package com.example.chess.models;

import com.example.http.HttpRequest;

public class Position {

    public final int x;
    public final int y;
    private static final String[] numberToChar = {"a","b","c","d","e","f","g","h"};

    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return numberToChar[x] + y;
    }

    //TODO
    public static Position fromRequest(HttpRequest request){
        return new Position(0,0);
    }

    public Position moveTo(Direction direction){
        return new Position(x + direction.x, y + direction.y);
    }
   
    public boolean equals(Position position){
        return position.x == x && position.y == y;
    }
}

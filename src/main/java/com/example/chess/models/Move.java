package com.example.chess.models;

import com.example.http.HttpMessage;

public class Move {

    public Position origin;
    public Position destination;

    public Move(Position from, Position to){
        this.origin = from;
        this.destination = to;
    }

    public static Move fromRequest(HttpMessage request){

        return new Move(null, null);
    }
}

package com.example.chess.models;

import com.example.http.HttpMessage;

public class Move {

    public Position origin;
    public Position destination;

    public static Move fromRequest(HttpMessage request){

        return new Move();
    }
}

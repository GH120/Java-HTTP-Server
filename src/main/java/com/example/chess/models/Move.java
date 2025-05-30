package com.example.chess.models;

import com.example.http.HttpMessage;

public class Move {

    public Position origin;
    public Position destination;
    public Event    event;

    public enum Event {
        ATTACK,       // Movimento padrão, conta como ataque em outra peça
        CHECK,        // Causa xeque
        CASTLING,     // Roque
        EN_PASSANT,   // En passant
        PROMOTION,    // Promoção de peão
        CHECKMATE,    // Xeque-mate
        MOVEMENT,     // Movimento Inofensivo do peão
        TWOTILESKIP   // Movimento de pulo do peão
    }

    public Move(Position from, Position to){
        this.origin      = from;
        this.destination = to;
        this.event       = Event.ATTACK;
    }

    public Move setEvent(Event event){
        this.event = event;
        return this;
    }

    public static Move fromRequest(HttpMessage request){

        return new Move(null, null);
    }
}

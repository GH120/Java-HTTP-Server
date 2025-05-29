package com.example.chess.models;

import com.example.http.HttpMessage;

public class Move {

    public Position origin;
    public Position destination;
    public Event    event;

    public enum Event {
        NORMAL,       // Movimento padrão
        CHECK,        // Causa xeque
        CASTLING,     // Roque
        EN_PASSANT,   // En passant
        PROMOTION,    // Promoção de peão
        CHECKMATE     // Xeque-mate
    }

    //TODO: adicionar campo Evento, um enum para categorizar algo especial (cheque, roque, en-passant...)

    public Move(Position from, Position to){
        this.origin      = from;
        this.destination = to;
        this.event       = Event.NORMAL;
    }

    public static Move fromRequest(HttpMessage request){

        return new Move(null, null);
    }
}

package com.example.chess.models;

import java.nio.charset.StandardCharsets;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class Move {

    public Position origin;
    public Position destination;
    public Event    event;

    public enum Event {
        ATTACK,       // Movimento padrão, conta como ataque em outra peça
        CASTLING,     // Roque
        EN_PASSANT,   // En passant
        PROMOTION,    // Promoção de peão
        MOVEMENT,     // Movimento Inofensivo do peão
        TWOTILESKIP;  // Movimento de pulo do peão

        public Piece target;

        public Event setTarget(Piece target) {
            this.target = target;
            return this;
        }
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

    public static Move fromRequest(HttpRequest request){

        try{

            JsonNode node = Json.parse(new String(request.getBody(), StandardCharsets.US_ASCII));

            JsonNode moveInfo = node.get("move");

            System.out.println(moveInfo.asText());

            String[] tilesInNotation =  moveInfo.asText().split(" ");

            return new Move(
                        Position.fromNotation(tilesInNotation[0]),
                        Position.fromNotation(tilesInNotation[1])
                   );
        }
        catch(Exception e){

            System.out.println("NÃO FOI POSSÍVEL CRIAR JOGADA");

            e.printStackTrace();

            return null;
        }
    }

    @Override
    public boolean equals(Object move){
        return ((Move) move).origin.equals(origin) && ((Move) move).destination.equals(destination);
    }
}

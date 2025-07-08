package com.example.chess.models;

import java.nio.charset.StandardCharsets;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

            JsonNode origin = moveInfo.get("origin");
            JsonNode destination = moveInfo.get("destination");

            // System.out.println(moveInfo.asText());

            // String[] tilesInNotation =  moveInfo.asText().split(" ");

            // return new Move(
            //             Position.fromNotation(tilesInNotation[0]),
            //             Position.fromNotation(tilesInNotation[1])
            //        );

            return new Move(
                new Position(Integer.parseInt(origin.get("x").asText()), Integer.parseInt(origin.get("y").asText())), 
                new Position(Integer.parseInt(destination.get("x").asText()), Integer.parseInt(destination.get("y").asText()))
            );
        }
        catch(Exception e){

            System.out.println("NÃO FOI POSSÍVEL CRIAR JOGADA");

            e.printStackTrace();

            return null;
        }
    }

   public String toJson() {
        // 1. Cria um ObjectMapper (se você não tiver um já configurado)
        ObjectMapper mapper = new ObjectMapper();
        
        // 2. Converte o Move para um ObjectNode (mutável)
        ObjectNode moveNode = mapper.valueToTree(this);
        
        // 3. Substitui os nós de Position pelos seus toString()
        moveNode.put("origin", origin.toString());
        moveNode.put("destination", destination.toString());
        
        // 4. Se houver peça alvo no evento, trata também
        if (event.target != null) {
            moveNode.put("target", event.target.toString());
        }
        
        // 5. Converte de volta para String JSON
        try {
            return mapper.writeValueAsString(moveNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize move", e);
        }
    }

    @Override
    public boolean equals(Object move){
        return ((Move) move).origin.equals(origin) && ((Move) move).destination.equals(destination);
    }

    public String toString(){
        return origin.toString().concat(" ").concat(destination.toString());
    }
}

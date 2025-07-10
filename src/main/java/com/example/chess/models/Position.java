package com.example.chess.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

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
    public static Position fromRequest(HttpRequest request) throws JsonProcessingException, IOException{

        String info = new String(request.getBody(), StandardCharsets.US_ASCII);

        JsonNode node = Json.parse(info).get("coordinates");

        return new Position(Integer.parseInt(node.get("x").asText()), Integer.parseInt(node.get("y").asText()));
    }

    public static Position fromNotation(String notation){
        int x = 0;
        int y = Integer.parseInt(notation.substring(1));

        for(String letter = numberToChar[x]; !notation.startsWith(letter); letter = numberToChar[x]){
            x++;

            if(x > 8) break;
        };

        return new Position(x, y);
    }

    public Position moveTo(Direction direction){
        return new Position(x + direction.x, y + direction.y);
    }
   
    public boolean equals(Position position){
        return position.x == x && position.y == y;
    }
}

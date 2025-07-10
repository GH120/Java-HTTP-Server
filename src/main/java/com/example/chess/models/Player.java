package com.example.chess.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;

public class Player {
    
    public String     name;
    public Integer    ELO;
    public String     address;

    public Player(String name){
        this.name = name;
        this.ELO = 1000;
        this.address = "Does not matter";
    }

    //Refazer para novo campo 
    public static Player fromRequest(HttpRequest request) throws JsonParseException, IOException{

        try{

            System.out.println(new String(request.getBody(), StandardCharsets.US_ASCII));
            
            JsonNode node = Json.parse(new String(request.getBody(), StandardCharsets.US_ASCII));
            
            JsonNode playerInfo = node.get("player");
            
            return Json.fromJson(playerInfo, Player.class);
        }
        catch(Exception e){

            System.out.println("Player da requisição não existe ou não foi encontrado, usando players de teste global Black e White");

            JsonNode node = Json.parse(new String(request.getBody(), StandardCharsets.US_ASCII));
            
            JsonNode playerInfo = node.get("player");

            return new Player(playerInfo.get("name").asText());
        }
    }

    public String toString(){
        return name + "@" + address;
    }
}

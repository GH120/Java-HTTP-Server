package com.example.chess.models;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class Player {
    
    public String     name;
    public Integer    ELO;
    public String     address;

    //Refazer para novo campo 
    public static Player fromRequest(HttpRequest request) throws Exception{
        JsonNode node = Json.parse(request.getBody());

        JsonNode playerInfo = node.get("player");

        return Json.fromJson(playerInfo, Player.class);
    }

    public String toString(){
        return name + "@" + address;
    }
}

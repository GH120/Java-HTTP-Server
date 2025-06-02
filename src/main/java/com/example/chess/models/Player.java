package com.example.chess.models;

import java.util.Set;

import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class Player {
    
    public String     name;
    public Integer    ELO;
    public String     address;

    //Refazer para novo campo 
    public static Player fromRequest(HttpRequest request) throws Exception{
        System.out.println(request.getBody());
        JsonNode info = Json.parse(request.getBody());

        return Json.fromJson(info, Player.class);
    }

    public String toString(){
        return name + "@" + address;
    }
}

package com.example.chess.models;

import com.example.http.HttpMessage;
import com.example.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class Player {
    
    public String  name;
    public Integer ELO;
    public String  address;

    public static Player fromRequest(HttpMessage request) throws Exception{
        System.out.println(request.getBody());
        JsonNode info = Json.parse(request.getBody());

        return Json.fromJson(info, Player.class);
    }

    public String toString(){
        return name + "@" + address;
    }
}

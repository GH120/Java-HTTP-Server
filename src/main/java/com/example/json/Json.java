package com.example.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json {

    private static ObjectMapper myObjectMapper = defaulObjectMapper();

    private static ObjectMapper defaulObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();

        //Não crasha se tiver alguma propriedade faltando
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    public static JsonNode parse(String jsonSrc) throws IOException{
        return myObjectMapper.readTree(jsonSrc);
    }

    public static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException{
        return myObjectMapper.treeToValue(node,clazz);
    }

    public static JsonNode toJson(Object obj){
        return myObjectMapper.valueToTree(obj);
    }

    //Ver se isso não pode ser simplificado para apenas um generateJson
    public static byte[] from(Object obj) throws JsonProcessingException{
        return stringify(toJson(obj)).getBytes();
    }

    private static String generateJson(Object obj) throws JsonProcessingException{
        ObjectWriter objectWriter = myObjectMapper.writer();

        return objectWriter.writeValueAsString(obj);
    }

    public static String stringify(JsonNode node) throws JsonProcessingException{
        return generateJson(node);
    }
}

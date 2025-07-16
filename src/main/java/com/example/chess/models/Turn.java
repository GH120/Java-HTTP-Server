package com.example.chess.models;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


public record Turn(

    @JsonProperty("id")
    Integer id,

    @JsonProperty("move")
    Move move,

    @JsonProperty("black")
    Player black,

    @JsonProperty("white")
    Player white,

    @JsonProperty("currentPlayer")
    Player currentPlayer,

    @JsonProperty("timeTaken")
    Integer timeTaken,

    @JsonSerialize(keyUsing = PlayerKeySerializer.class)
    Map<Player, Integer> timeRemaining,

    @JsonProperty("casualties")
    List<Piece> casualties,

    @JsonProperty("gamestate")
    ChessMatch.GameState gamestate,

    @JsonProperty("board")
    Piece[][] board
) {}

class PlayerKeySerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(Player value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName(value.name); // ou qualquer outra forma de identificação
    }
}
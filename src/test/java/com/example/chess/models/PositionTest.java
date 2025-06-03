package com.example.chess.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PositionTest {
    @Test
    void testFromNotation() {
        String notation = "e4";

        assert(Position.fromNotation(notation).equals(new Position(4, 4)));
    }
}

package com.example.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class ChessNotationParserTest {

    private ChessNotationLexer lexer;
    private ChessNotationParser parser;

    @BeforeEach
    void setup() {
        lexer = new ChessNotationLexer();
        parser = new ChessNotationParser();
    }

    @Test
    void testValidMovesSequence() {
        String[] moves = {
                "e4", "Nf3", "d4", "Nxd4", "Nxc6", "e5", "Qe2", "c4", "Nd2", "Qe3", "Bd3", "Qg3", "O-O",
                "Nb3", "a3", "Nxc5", "Be3", "b4", "Bd4", "Bxe5", "f4", "fxe5", "Qxd3", "Qd7", "Qxc6",
                "Kh1", "Rfe1", "Qxc7", "Qxa7", "h3", "Qc5", "Qxe5", "Qe3", "Rac1", "Qg3", "b5", "a4",
                "Rb1", "b6", "b7", "Qxg5"
        };

        for (String move : moves) {
            LinkedList<Token> tokens = lexer.tokenize(move);

            assertDoesNotThrow(() -> parser.parse(tokens), "Erro ao analisar o lance: " + move);
        }
    }
}

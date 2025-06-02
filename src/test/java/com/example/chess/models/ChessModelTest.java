package com.example.chess.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.chess.models.chesspieces.Pawn;

class ChessModelTest {
    private ChessModel model;

    @BeforeEach
    void setUp() {
        model = new ChessModel();
    }

    @Test
    void testPlay_NormalMove() {
        Piece pawn = model.getPiece(new Position(0, 1));
        Move move = new Move(pawn.position, new Position(0, 2));
        
        model.play(pawn, move);
        
        assertNull(model.getPiece(new Position(0, 1)));
        assertEquals(pawn, model.getPiece(new Position(0, 2)));
        assertEquals(1, model.getTurn());
    }

    @Test
    void testPlay_Capture() {
        // Move peão para posição de captura
        Piece pawn = model.getPiece(new Position(0, 1));
        model.play(pawn, new Move(pawn.position, new Position(0, 3)));
        
        // Peão preto na diagonal
        Piece blackPawn = model.getPiece(new Position(1, 6));
        Move capture = new Move(blackPawn.position, new Position(0, 3));
        
        int initialPieces = model.getAllPieces(PlayerColor.WHITE).size();
        model.play(blackPawn, capture);
        
        assertEquals(initialPieces - 1, model.getAllPieces(PlayerColor.WHITE).size());
        assertTrue(model.getCasualties().contains(pawn));
    }

    @Test
    void testRevertLastMove() {
        Piece pawn = model.getPiece(new Position(0, 1));
        Move move = new Move(pawn.position, new Position(0, 2));
        
        model.play(pawn, move);
        model.revertLastMove();
        
        assertEquals(pawn, model.getPiece(new Position(0, 1)));
        assertNull(model.getPiece(new Position(0, 2)));
        assertEquals(0, model.getTurn());
    }

    @Test
    void testEnPassant() {

        Piece blackPawn = model.getPiece(new Position(0,6));
        model.play(blackPawn, new Move(blackPawn.position, new Position(1, 3)));

        // Peão branco avança 2 casas
        Piece whitePawn = model.getPiece(new Position(0, 1));
        model.play(whitePawn, new Move(whitePawn.position, new Position(0, 3)));
        
        // Peão preto faz en passant
        Move enPassant = new Move(blackPawn.position, new Position(0, 2));
        enPassant.setEvent(Move.Event.EN_PASSANT.setTarget(whitePawn));
        
        model.play(blackPawn, enPassant);
        
        assertNull(model.getPiece(new Position(0, 3))); // Peão branco capturado
        assertEquals(blackPawn, model.getPiece(new Position(0, 2)));
    }
}

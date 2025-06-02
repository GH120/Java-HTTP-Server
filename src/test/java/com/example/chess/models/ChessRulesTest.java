package com.example.chess.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;
import com.example.chess.models.chesspieces.Rook;
import com.example.chess.models.gamestart.DefaultStartingPieces;

import java.util.ArrayList;
import java.util.List;

class ChessRulesTest {
    private ChessModel model;
    private ChessRules rules;

    @BeforeEach
    void setUp() {
        model = new ChessModel(new DefaultStartingPieces());
        rules = new ChessRules();
    }

    @Test
    void testValidateMoves_PawnInitialMove() {
        Pawn pawn = (Pawn) model.getPiece(new Position(0, 1)); // Peão branco
        List<Move> moves = rules.validateMoves(model, pawn, pawn.defaultMoves(model.getBoard()));
        
        assertEquals(2, moves.size()); // Deve permitir 1 ou 2 casas para frente
        assertTrue(moves.contains(new Move(pawn.position, new Position(0, 2))));
        assertTrue(moves.contains(new Move(pawn.position, new Position(0, 3))));
    }

    @Test
    void testAddCastlingMoves_KingSide() {
        // Remove peças entre rei e torre
        model.kill(model.getPiece(new Position(5, 0))); // Bispo
        model.kill(model.getPiece(new Position(6, 0))); // Cavalo
        
        King king = (King) model.getPiece(new Position(4, 0)); // Rei branco
        List<Move> moves = king.defaultMoves(model.getBoard());
        rules.validateMoves(model, king, moves);
        
        // Deve incluir roque pequeno
        assertTrue(moves.stream().anyMatch(m -> 
            m.event == Move.Event.CASTLING && 
            m.destination.equals(new Position(6, 0)))
        );
    }

    @Test
    void testIsInCheck_KingUnderAttack() {
        // Simula situação de xeque
        model.kill(model.getPiece(new Position(3, 0))); // Remove rainha branca
        model.insertPiece(new Queen(new Position(3, 1), PlayerColor.BLACK)); // Rainha preta atacando rei
        
        assertTrue(rules.isInCheck(model, PlayerColor.WHITE));
    }

    @Test
    void testWouldCauseSelfCheck() {
        King king = model.findKing(PlayerColor.WHITE);
        Move suicidalMove = new Move(king.position, new Position(4, 1)); // Move rei para frente
        
        // Coloca uma torre inimiga na coluna
        model.insertPiece(new Rook(new Position(4, 5), PlayerColor.BLACK));

        ArrayList<Move> moves = new ArrayList<>();

        moves.add(suicidalMove);
        
        assertTrue(rules.validateMoves(model, king, moves).size() == 0);
    }

    @Test
    void testIsDraw_Stalemate() {
        // Configura situação de afogamento


        StartingPieces setup = new StartingPieces() {
            
            public void populateBoard(ChessModel model){
                model.insertPiece(new King(new Position(0, 0), PlayerColor.WHITE));
                model.insertPiece(new King(new Position(7, 7), PlayerColor.BLACK));
                model.insertPiece(new Queen(new Position(1, 1), PlayerColor.BLACK));
            }
            
        };

        model = new ChessModel(setup);
        
        assertTrue(rules.isDraw(model, PlayerColor.WHITE));
    }
}

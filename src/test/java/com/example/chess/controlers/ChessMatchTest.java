package com.example.chess.controlers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.chess.models.*;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;

class ChessMatchTest {
    private ChessMatch match;
    private Player white;
    private Player black;

    @BeforeEach
    void setUp() {
        white = new Player();
        black = new Player();
        match = new ChessMatch(white, black);
    }

    @Test
    void testPlayMove_Valid() throws Exception {
        Move move = new Move(new Position(1, 0), new Position(2, 0)); // Peão branco
        
        match.playMove(white, move);
        
        assertEquals(ChessMatch.GameState.NORMAL, match.getState());
    }

    @Test
    void testPlayMove_InvalidTurn() {
        Move move = new Move(new Position(6, 0), new Position(5, 0)); // Peão preto no turno errado
        
        assertThrows(ChessMatch.NotPlayerTurn.class, () -> {
            match.playMove(black, move);
        });
    }

    @Test
    void testCheckmateDetection() {
        // Configura xeque-mate (Fool's Mate)
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(1, 5))); // Peão f2
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(1, 6))); // Peão g2
        
        // Rainha preta para h4
        match.getChessModel().insertPiece(
            new Queen(new Position(4, 7), PlayerColor.BLACK)
        );
        
        // Move rainha para h4 dando xeque-mate
        Move mateMove = new Move(new Position(4, 7), new Position(4, 3));
        
        assertDoesNotThrow(() -> match.playMove(black, mateMove));
        assertEquals(ChessMatch.GameState.CHECKMATE, match.getState());
    }

    @Test
    void testPromotionFlow() {
        // Configura promoção
        ChessModel model = match.getChessModel();
        model.kill(model.getPiece(new Position(1, 0))); // Remove peão branco
        Pawn pawn = new Pawn(new Position(6, 0), PlayerColor.WHITE); // Peão perto de promover
        model.insertPiece(pawn, pawn.position);
        
        Move promotionMove = new Move(pawn.position, new Position(7, 0));
        promotionMove.setEvent(Move.Event.PROMOTION);
        
        assertDoesNotThrow(() -> match.playMove(white, promotionMove));
        assertEquals(ChessMatch.GameState.PROMOTION, match.getState());
        
        // Testa escolha de promoção
        assertDoesNotThrow(() -> match.choosePromotion(Pawn.Promotion.QUEEN));
        assertEquals(Queen.class, model.getPiece(new Position(7, 0)).getClass());
    }
}
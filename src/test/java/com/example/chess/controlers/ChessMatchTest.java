package com.example.chess.controlers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.chess.models.*;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;
import com.example.chess.models.chesspieces.Rook;

class ChessMatchTest {
    private ChessMatch match;
    private Player white;
    private Player black;

    @BeforeEach
    void setUp() {
        white = new Player("Guest1");
        black = new Player("Guest2");
        match = new ChessMatch(white, black);
    }

    @Test
    void testPlayMove_Valid() throws Exception {
        Move move = new Move(new Position(1, 1), new Position(1, 2)); // Peão branco
        
        match.playMove(white, move);
        
        assertEquals(ChessMatch.GameState.NORMAL, match.getState());
    }

    @Test
    void testPlayMove_InvalidTurn() {
        Move move = new Move(new Position(0, 6), new Position(0, 5)); // Peão preto no turno errado
        
        assertThrows(ChessMatch.NotPlayerTurn.class, () -> {
            match.playMove(black, move);
        });
    }

    @Test
    void testCheckDetection() {
        // Configura xeque-mate (Fool's Mate)
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(1, 5))); // Peão f2
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(1, 6))); // Peão g2
        

        assertDoesNotThrow(()-> {
            match.playMove(white, new Move(new Position(0,1), new Position(0,3)));
        });

        // Rainha preta para h4
        match.getChessModel().insertPiece(
            new Queen(new Position(7, 4), PlayerColor.BLACK)
        );
        
        // Move rainha para h4 dando xeque-mate
        Move mateMove = new Move(new Position(7, 4), new Position(4, 1));
        
        assertDoesNotThrow(() -> match.playMove(black, mateMove));
        assertEquals(ChessMatch.GameState.CHECK, match.getState());
    }

    @Test
    void testCheckMateDetection() {
        // Configura xeque-mate (Fool's Mate)
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(5, 1))); // Peão f2
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(6, 1))); // Peão g2
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(5, 0))); // bispo
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(3, 0))); // rainha
        match.getChessModel().kill(match.getChessModel().getPiece(new Position(6, 0))); // CAVALO

        assertDoesNotThrow(()-> {
            match.playMove(white, new Move(new Position(0,1), new Position(0,3)));
        });

        // Rainha preta para h4
        match.getChessModel().insertPiece(
            new Queen(new Position(7, 4), PlayerColor.BLACK)
        );

        // Torre que protege a rainha no cheque
        match.getChessModel().insertPiece(
            new Rook(new Position(4, 5), PlayerColor.BLACK)
        );
        
        // Move rainha para h4 dando xeque-mate
        Move mateMove = new Move(new Position(7, 4), new Position(4, 1));
        
        try{

            match.playMove(black, mateMove);
        }
        catch(Exception e){
            
        }
        assertEquals(ChessMatch.GameState.CHECKMATE, match.getState());
    }

    @Test
    void testPromotionFlow() {
        // Configura promoção
        ChessModel model = match.getChessModel();
        model.kill(model.getPiece(new Position(0, 1))); // Remove peão branco
        Pawn pawn = new Pawn(new Position(0, 6), PlayerColor.WHITE); // Peão perto de promover
        model.insertPiece(pawn, pawn.position);
        
        Move promotionMove = new Move(pawn.position, new Position(1, 7));
        promotionMove.setEvent(Move.Event.PROMOTION);
        
        assertDoesNotThrow(() -> match.playMove(white, promotionMove));
        assertEquals(ChessMatch.GameState.PROMOTION, match.getState());
        
        // Testa escolha de promoção
        assertDoesNotThrow(() -> match.choosePromotion(Pawn.Promotion.QUEEN));
        assertEquals(Queen.class, model.getPiece(new Position(1, 7)).getClass());
    }
}
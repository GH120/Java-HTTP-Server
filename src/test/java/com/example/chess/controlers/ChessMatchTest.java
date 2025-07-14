package com.example.chess.controlers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;


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
    void testMatchSynchronization() throws InterruptedException {
        ChessMatch.MatchSynchronizer synchronizer = match.semaphor;

        AtomicBoolean moveProcessed = new AtomicBoolean(false);

        Thread waitingThread = new Thread(() -> {
            try {
                synchronizer.waitForMove();
                moveProcessed.set(true); // Marca que a thread foi desbloqueada
            } catch (InterruptedException e) {
                fail("Thread foi interrompida inesperadamente");
            }
        });

        waitingThread.start();

        // Aguarda para garantir que a thread está bloqueada
        Thread.sleep(200);

        assertFalse(moveProcessed.get(), "A thread não deveria ter processado o movimento ainda");

        // Agora notificamos a jogada
        synchronizer.notifyMove();

        // Aguarda a thread liberar
        waitingThread.join(1000); // Timeout de 1s

        assertTrue(moveProcessed.get(), "A thread deveria ter sido desbloqueada após notifyMove()");
    }

    @Test
    void testRealMatchSynchronization() throws Exception {
        ChessMatch.MatchSynchronizer synchronizer = match.semaphor;

        // Thread do jogador preto esperando sua vez
        Thread blackThread = new Thread(() -> {
            try {
                // Espera jogada do branco
                synchronizer.waitForMove();

                // Depois de notificado, faz sua jogada
                Move blackMove = new Move(new Position(1, 1), new Position(1, 3)); // e7 → e5

                match.playMove(black, blackMove);

                assertEquals(PlayerColor.WHITE, match.getChessModel().getCurrentColor());
            } catch (Exception e) {
                fail("Erro na thread do jogador preto: " + e.getMessage());
            }
        });

        blackThread.start();

        // Jogador branco joga e2 → e4
        Move whiteMove = new Move(new Position(7, 6), new Position(7, 4));
        match.playMove(white, whiteMove);

        // Após a jogada, notifica que o movimento foi feito
        synchronizer.notifyMove();
        
        Thread.sleep(10000);

        // Aguarda a thread do jogador preto terminar
        blackThread.join(1000);

        // Verifica se o estado é NORMAL após os dois lances
        assertEquals(ChessMatch.GameState.NORMAL, match.getState());
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
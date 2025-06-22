package com.example.chess.services;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.chess.models.*;
import com.example.chess.services.ChessMatch.GameState;

class MatchTimerTest {

    static class MockChessMatch extends ChessMatch {
        public int whiteTime = 5;
        public int blackTime = 5;
        public boolean timeoutTriggered = false;
        public Player lastPlayerUpdated;
        
        // Instâncias reais de Player
        private final Player whitePlayer = new Player();
        private final Player blackPlayer = new Player();

        public MockChessMatch() {
            super(new Player(), new Player()); // Super chamada vazia (será sobrescrito)
            
            // Configuração dos jogadores
            whitePlayer.name = "Jogador Branco";
            whitePlayer.ELO = 1500;
            whitePlayer.address = "192.168.1.1";
            
            blackPlayer.name = "Jogador Preto";
            blackPlayer.ELO = 1600;
            blackPlayer.address = "192.168.1.2";
        }

        @Override
        public Player getWhite() {
            return whitePlayer;
        }

        @Override
        public Player getBlack() {
            return blackPlayer;
        }

        @Override
        public Integer getTime(Player player) {
            return player == whitePlayer ? whiteTime : blackTime;
        }

        @Override
        public synchronized void updateTime(Player player, int time) {
            lastPlayerUpdated = player;
            if (player == whitePlayer) {
                whiteTime = time;
            } else {
                blackTime = time;
            }
        }

        @Override
        public void checkTimeOut() {
            if (whiteTime <= 0 || blackTime <= 0) {
                timeoutTriggered = true;
                super.checkTimeOut();
            }
        }

        @Override
        public Player getPlayer(PlayerColor color) {
            return color == PlayerColor.WHITE ? whitePlayer : blackPlayer;
        }
    }

    private MockChessMatch mockMatch;
    private MatchTimer timer;

    @BeforeEach
    void setUp() {
        mockMatch = new MockChessMatch();
        timer = new MatchTimer(mockMatch);
    }

    @Test
    void testPlayerInstances() {
        Player white = mockMatch.getWhite();
        Player black = mockMatch.getBlack();
        
        assertAll(
            () -> assertEquals("Jogador Branco", white.name),
            () -> assertEquals(1500, white.ELO),
            () -> assertEquals("Jogador Preto", black.name),
            () -> assertEquals(1600, black.ELO)
        );
    }

     @Test
    @DisplayName("Deve decrementar tempo do jogador ativo")
    void testTimeDecrement() throws InterruptedException {
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        
        Thread.sleep(1500); // Espera 1.5 segundos
        
        assertEquals(4, mockMatch.whiteTime, "Tempo do jogador branco deveria ser decrementado");
        assertEquals(mockMatch.getWhite(), mockMatch.lastPlayerUpdated);
    }

    @Test
    @DisplayName("Deve decrementar tempo do jogador ativo")
    void testTimeDecrement4Segundos() throws InterruptedException {
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        
        Thread.sleep(4500); // Espera 4.5 segundos
        
        assertEquals(1, mockMatch.whiteTime, "Tempo do jogador branco deveria ser decrementado");
        assertEquals(mockMatch.getWhite(), mockMatch.lastPlayerUpdated);
    }

    @Test
    @DisplayName("Deve cancelar timer anterior ao receber novo movimento")
    void testTimerCancellation() throws InterruptedException {
        // Primeiro movimento (branco)
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        Thread.sleep(500); // Espera 0.5s (não decrementa ainda)
        
        // Segundo movimento (preto) - deve cancelar o timer do branco
        timer.onMoveExecuted(new Move(new Position(7, 1), new Position(6, 1)), PlayerColor.BLACK);
        Thread.sleep(1200); // Espera 1.2s
        
        assertEquals(5, mockMatch.whiteTime, "Tempo do branco NÃO deveria mudar (timer cancelado)");
        assertEquals(4, mockMatch.blackTime, "Tempo do preto deveria ser decrementado");
    }

    @Test
    @DisplayName("Deve detectar timeout quando tempo chegar a zero")
    void testTimeoutDetection() throws InterruptedException {
        mockMatch.whiteTime = 1; // Configura para timeout no próximo decremento
        
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        Thread.sleep(1500); // Espera 1.5 segundos
        
        assertTrue(mockMatch.timeoutTriggered, "Timeout deveria ser detectado");
        assertEquals(0, mockMatch.whiteTime);
    }

    @Test
    @DisplayName("Deve parar todos os timers quando jogo terminar")
    void testShutdownOnGameEnd() {
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        timer.onGameStateChanged(GameState.CHECKMATE);
        
        // Verifica se o executor foi parado
        assertTrue(((ScheduledThreadPoolExecutor) timer.getExecutor()).isShutdown());
    }
}
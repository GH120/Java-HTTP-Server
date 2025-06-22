package com.example.chess.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.chess.models.*;
import com.example.chess.services.ChessMatch.GameState;

class MatchTimerTest {

    private ChessMatch match;
    private MatchTimer timer;

    @BeforeEach
    void setUp() {
        // Cria dois jogadores "aleatórios"
        Player white = new Player();
        white.name    = "Player_" + ThreadLocalRandom.current().nextInt(1000);
        white.ELO     = ThreadLocalRandom.current().nextInt(1200, 2000);
        white.address = "192.168.0." + ThreadLocalRandom.current().nextInt(1, 255);

        Player black = new Player();
        black.name    = "Player_" + ThreadLocalRandom.current().nextInt(1000);
        black.ELO     = ThreadLocalRandom.current().nextInt(1200, 2000);
        black.address = "192.168.0." + ThreadLocalRandom.current().nextInt(1, 255);

        match = new ChessMatch(white, black);
        timer = new MatchTimer(match);
        match.addObserver(timer);
    }

    @Test
    @DisplayName("Deve iniciar decremento de tempo após jogada")
    void testDecrementaTempoAposJogada() throws InterruptedException {
        Player white = match.getWhite();
        int tempoInicial = match.getTime(white); // Deve ser 10

        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);

        Thread.sleep(1500);

        int tempoFinal = match.getTime(white);
        assertTrue(tempoFinal < tempoInicial, "Tempo deveria ter sido decrementado");
    }

    @Test
    @DisplayName("Deve iniciar decremento de tempo após jogada")
    void testDecrementa5sAposJogada() throws InterruptedException {
        Player white = match.getWhite();
        int tempoInicial = match.getTime(white); // Deve ser 10

        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);

        Thread.sleep(5100);

        int tempoFinal = match.getTime(white);
        assertTrue(tempoFinal == tempoInicial-5, "Tempo deveria ter sido decrementado");
    }

    @Test
    @DisplayName("Deve encerrar executor após fim da partida")
    void testEncerramentoDoExecutor() {
        timer.onMoveExecuted(new Move(new Position(2, 1), new Position(3, 1)), PlayerColor.WHITE);
        timer.onGameStateChanged(GameState.CHECKMATE);

        assertTrue(((ScheduledThreadPoolExecutor) timer.getExecutor()).isShutdown());
    }
}

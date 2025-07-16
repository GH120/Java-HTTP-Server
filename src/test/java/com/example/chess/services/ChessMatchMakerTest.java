package com.example.chess.services;

import com.example.chess.models.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChessMatchMakerTest {

    @Test
    public void testRegistrationWithShortInterval() throws InterruptedException {
        ChessMatchMaker matchMaker = ChessMatchMaker.getInstance();
        
        // Criar dois jogadores
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        
        // Latch para sincronizar as threads
        CountDownLatch latch = new CountDownLatch(2);
        
        // Variáveis para armazenar os tempos
        long[] registrationTimes = new long[2];
        
        // Thread para o primeiro jogador
        Thread thread1 = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            matchMaker.findDuel(player1);
            registrationTimes[0] = System.currentTimeMillis() - startTime;
            latch.countDown();
        });
        
        // Thread para o segundo jogador
        Thread thread2 = new Thread(() -> {
            try {
                // Espera ~400ms antes de registrar o segundo jogador
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long startTime = System.currentTimeMillis();
            matchMaker.findDuel(player2);
            registrationTimes[1] = System.currentTimeMillis() - startTime;
            latch.countDown();
        });
        
        // Inicia as threads
        thread1.start();
        thread2.start();
        
        // Aguarda até 2 segundos para o teste completar
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        
        // Imprime os tempos
        System.out.println("Tempo de registro Player1: " + registrationTimes[0] + "ms");
        System.out.println("Tempo de registro Player2: " + registrationTimes[1] + "ms");
        
        try{

            // Verifica se a partida foi criada
            assertNotNull(ChessMatchManager.getInstance().getMatchFromPlayer(player1));
            assertNotNull(ChessMatchManager.getInstance().getMatchFromPlayer(player2));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        // Verifica se os jogadores foram removidos da lista de espera
        assertFalse(matchMaker.isPlayerWaiting(player1));
        assertFalse(matchMaker.isPlayerWaiting(player2));
        
    }
    
    // Método auxiliar para verificar se um jogador está esperando (precisa ser adicionado ao ChessMatchMaker)
    /*
    public boolean isPlayerWaiting(Player player) {
        return waitingPlayers.containsKey(player);
    }
    */
}
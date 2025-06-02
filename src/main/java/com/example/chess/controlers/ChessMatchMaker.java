package com.example.chess.controlers;

import java.util.LinkedList;
import java.util.Queue;

import com.example.chess.models.Player;

//Precisa receber também outputstream
//Pensando em criar objeto que contém o jogador, seu endereço e seu socket...
public class ChessMatchMaker {

    private static ChessMatchMaker instance;
    private final Queue<Player> waitingPlayers;

    private ChessMatchMaker() {
        waitingPlayers = new LinkedList<>();
    }

    public static synchronized ChessMatchMaker getInstance() {
        if (instance == null) {
            instance = new ChessMatchMaker();
        }
        return instance;
    }

    public synchronized void findDuel(Player player) {
        if (waitingPlayers.isEmpty()) {
            // Ninguém esperando, adiciona o player à fila
            waitingPlayers.add(player);
            System.out.println("Player added to waiting queue: " + player.name);
        } else {
            // Encontrou adversário
            Player opponent = waitingPlayers.poll();
            System.out.println("Match found: " + player.name + " vs " + opponent.name);

            ChessMatchManager.getInstance().addMatch(new ChessMatch(player,opponent));

            //Mandar página html

            // Aqui você pode também notificar os jogadores via socket ou resposta HTTP futura
        }
    }
}

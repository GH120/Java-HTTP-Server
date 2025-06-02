package com.example.chess.controlers;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import com.example.chess.api.MatchWatcher;
import com.example.chess.models.Player;
import com.example.chess.models.PlayerColor;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

//Precisa receber também outputstream
//Pensando em criar objeto que contém o jogador, seu endereço e seu socket...
public class ChessMatchMaker {

    private static ChessMatchMaker    instance;
    private final Queue<Player>       waitingPlayers;
    private final Queue<OutputStream> outputConnections;

    private ChessMatchMaker() {
        waitingPlayers    = new LinkedList<>();
        outputConnections = new LinkedList<>();
    }

    public static synchronized ChessMatchMaker getInstance() {
        if (instance == null) {
            instance = new ChessMatchMaker();
        }
        return instance;
    }

    public synchronized void findDuel(Player player, OutputStream output) {
        if (waitingPlayers.isEmpty()) {
            // Ninguém esperando, adiciona o player à fila
            waitingPlayers.add(player);
            outputConnections.add(output);
            System.out.println("Player added to waiting queue: " + player.name);

            // try{
            //     HttpStreamWriter.send(waitingResponse(), output); //Vai dar conflito com a outra mensagem enviada...
            // }
            // catch(Exception e){
            //     System.err.println("Erro na criação da partida" + e.getMessage());
            // }
        } else {
            // Encontrou adversário
            Player opponent = waitingPlayers.poll();
            OutputStream opponentOutput = outputConnections.poll();
            System.out.println("Match found: " + player.name + " vs " + opponent.name);


            //Cria partida
            ChessMatch match = new ChessMatch(player, opponent);

            //Adiciona observers a partida (criar um factory pra isso)
            match.addObserver(new MatchWatcher(PlayerColor.WHITE, output));
            match.addObserver(new MatchWatcher(PlayerColor.BLACK, opponentOutput));

            ChessMatchManager.getInstance().addMatch(match);

            try{
                HttpStreamWriter.send(startResponse(), output);
                HttpStreamWriter.send(startResponse(), opponentOutput);
            }
            catch(Exception e){
                System.err.println("Erro na criação da partida" + e.getMessage());
            }
            // Aqui você pode também notificar os jogadores via socket ou resposta HTTP futura
        }
    }

    private HttpResponse startResponse(){
        return HttpResponse.OK("{\"status\":\"match_started\"}".getBytes(), "application/json");
    }

    // private HttpResponse waitingResponse(){
    //     return HttpResponse.OK("{\"status\":\"waiting\"}".getBytes(), "application/json");
    // }
}

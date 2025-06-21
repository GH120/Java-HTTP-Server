package com.example.chess.controlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.example.chess.api.MatchSynchronizer;
import com.example.chess.models.Player;
import com.example.chess.models.PlayerColor;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamReader;
import com.example.parser.HttpStreamWriter;


//Boa sugestão do deepseek: Adicionar cancelMatch para players desistentes sairem do waiting players
public class ChessMatchMaker {

    private static ChessMatchMaker    instance;
    private final  Map<Player, MatchIntention> waitingPlayers;


    private final int TIMEOUT = 120;

    private ChessMatchMaker() {
        waitingPlayers    = new HashMap<>();
    }

    public static synchronized ChessMatchMaker getInstance() {
        if (instance == null) {
            instance = new ChessMatchMaker();
        }
        return instance;
    }

    //Primeiro teria que entrar num objeto para iniciar a partida

    //Primeiro player chama o find duel, e fica na espera (wait)
    //Segundo  player chama o find duel, encontra o usuário, e notifica a todos a criação da partida
    //Teria que ter um método syncronized para o wait? Acho que não, porque vários jogadores poderiam existir e requisitar o wait paralelamente
    //Seria bom criar um objeto player lock, e criar ele quando o player ficar esperando, guardando um mapa de player locks
    //Isso também possibilitaria uma generalização, onde o player poderia escolher a lista de seus adversários, e o lock desse adversário seria liberado.

    public void findDuel(Player player, InputStream input, OutputStream output) {

        System.out.println("IS empty? " + waitingPlayers.isEmpty());

        if (waitingPlayers.isEmpty()) {
            // Ninguém esperando, adiciona o player à fila

            var matchIntention = new MatchIntention();

            waitingPlayers.put(player, matchIntention);

            System.out.println("Player added to waiting queue: " + player.name);

            matchIntention.findOpponent();

            try{
                HttpStreamWriter.send(startResponse(), output);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        } else {
            // Encontrou adversário
            Player opponent = waitingPlayers.keySet().iterator().next();

            System.out.println("Match found: " + player.name + " vs " + opponent.name);


            //Cria partida
            ChessMatch match = new ChessMatch(player, opponent);

            ChessMatchManager.getInstance().addMatch(match);

            try{
                HttpStreamWriter.send(startResponse(), output);

                //Sinaliza para o adversário que ele foi escolhido
                waitingPlayers.get(opponent).matchFound();

                // Retira adversário e jogador do mapa de espera 
                waitingPlayers.remove(player);
                waitingPlayers.remove(opponent);
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

    private class MatchIntention{

        public synchronized void findOpponent(){
            try{
                wait();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        public synchronized void matchFound(){
            notifyAll();
        }
    }
}

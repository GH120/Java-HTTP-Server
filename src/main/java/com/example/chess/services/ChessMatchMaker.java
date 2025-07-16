package com.example.chess.services;

import java.util.HashMap;
import java.util.Map;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Player;


//Boa sugestão do deepseek: Adicionar cancelMatch para players desistentes sairem do waiting players
public class ChessMatchMaker {

    private static ChessMatchMaker instance;

    private final  Map<Player, MatchIntention> waitingPlayers;

    private ChessMatchMaker() {
        waitingPlayers    = new HashMap<>();
    }

    public static ChessMatchMaker getInstance() {
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

    public void findDuel(Player player) {

        System.out.println("IS empty? " + waitingPlayers.isEmpty());

        if (waitingPlayers.isEmpty()) 
            putPlayerOnHold(player);        
        else 
            fetchAnyOpponent(player);
    }

    public void cancelDuel(Player player) {
        MatchIntention intention = waitingPlayers.remove(player);
        if (intention != null) {
            intention.matchFound(); // Libera a thread bloqueada
        }
    }

    private void putPlayerOnHold(Player player){

        // Ninguém esperando, adiciona o player à fila

        var matchIntention = new MatchIntention();

        waitingPlayers.put(player, matchIntention);

        System.out.println("Player added to waiting queue: " + player.name);

        matchIntention.findOpponent();
    }

    private void fetchAnyOpponent(Player player){
        // Encontrou adversário
        Player opponent = waitingPlayers.keySet().iterator().next();

        System.out.println("Match found: " + player.name + " vs " + opponent.name);


        //Cria partida
        ChessMatch match = new ChessMatch(player, opponent);

        ChessMatchManager.getInstance().addMatch(match);

        //Sinaliza para o adversário que ele foi escolhido
        waitingPlayers.get(opponent).matchFound();

        // Retira adversário e jogador do mapa de espera 
        waitingPlayers.remove(opponent);
       
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

    public boolean isPlayerWaiting(Player player){
        return waitingPlayers.containsKey(player);
    }
}

package com.example.chess.services;

import java.util.concurrent.ConcurrentHashMap;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Player;

public class ChessMatchManager {

    private static ChessMatchManager instance;
    private ConcurrentHashMap<String, ChessMatch>   matches;

    private ChessMatchManager() {
        matches = new ConcurrentHashMap<>();

        //Partida global para fins de teste
        Player p1 = new Player("White");
        Player p2 = new Player("Black");

        addMatch(new ChessMatch(p1, p2));
    }

    public static ChessMatchManager getInstance() {
        if (instance == null) {
            instance = new ChessMatchManager();
        }
        return instance;
    }

    public Player createGuest(){
        return new Player("Guest" + IDGenerator.nextId());
    }

    public void addMatch(ChessMatch match) {

        matches .put(match.getWhite().toString(), match);
        matches .put(match.getBlack().toString(), match);

        match.addObserver(new MatchTimer(match));
        match.addObserver(new MatchEndHandler(match));
        
    }

    public void removeMatch(ChessMatch match){

        matches.remove(match.getWhite().toString());
        matches.remove(match.getBlack().toString());
    }

    // métodos para buscar ou gerenciar partidas
    public ChessMatch getMatchFromPlayer(Player player) throws MatchNotFound {

        return matches.get(player.toString());
    }

    public class MatchNotFound extends Exception {

        public MatchNotFound(){
            super("Partida não encontrada");
        }
    }

    private class IDGenerator{

        private static long currentID = 0;

        //Synchronized estava travando muito, depois ver casos de sincronização
        public static long nextId(){
            return ++currentID;
        }
    }

}

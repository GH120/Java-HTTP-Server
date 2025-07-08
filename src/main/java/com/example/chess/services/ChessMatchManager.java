package com.example.chess.services;

import java.util.concurrent.ConcurrentHashMap;

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

    public static synchronized ChessMatchManager getInstance() {
        if (instance == null) {
            instance = new ChessMatchManager();
        }
        return instance;
    }

    public void addMatch(ChessMatch match) {

        matches .put(match.getWhite().toString(), match);
        matches .put(match.getBlack().toString(), match);

        match.addObserver(new MatchTimer(match));
        match.addObserver(new MatchEndHandler(match));
        
    }

    // métodos para buscar ou gerenciar partidas
    public ChessMatch getMatchFromPlayer(Player player) throws MatchNotFound {

        matches.keySet().stream().forEach(key -> System.out.println(key));
        System.out.println(player);

        return matches.get(player.toString());
    }

    public void removeMatch(ChessMatch match){

        matches.remove(match.getWhite().toString());
        matches.remove(match.getBlack().toString());
    }

    public class MatchNotFound extends Exception {

        public MatchNotFound(){
            super("Partida não encontrada");
        }
    }

}

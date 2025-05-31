package com.example.chess.controlers;

import java.util.concurrent.ConcurrentHashMap;
import com.example.chess.models.Player;

public class ChessMatchManager {

    private static ChessMatchManager instance;
    private ConcurrentHashMap<String, ChessMatch> matches;

    private ChessMatchManager() {
        matches = new ConcurrentHashMap<>();
    }

    public static synchronized ChessMatchManager getInstance() {
        if (instance == null) {
            instance = new ChessMatchManager();
        }
        return instance;
    }

    public void addMatch(ChessMatch match) {
        
        matches.put(match.getWhite().toString(), match);
        matches.put(match.getBlack().toString(), match);
    }

    // métodos para buscar ou gerenciar partidas
    public ChessMatch getMatchFromPlayer(Player player){

        return matches.get(player.toString());
    }
}

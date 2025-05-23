package com.example.chess.controlers;

import java.util.concurrent.ConcurrentHashMap;
import com.example.chess.models.ChessMatch;

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
        String matchId = match.getWhite().name + "_vs_" + match.getBlack().name;
        matches.put(matchId, match);
    }

    // métodos para buscar ou gerenciar partidas
}

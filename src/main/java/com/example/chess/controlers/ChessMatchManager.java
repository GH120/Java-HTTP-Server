package com.example.chess.controlers;

import java.util.concurrent.ConcurrentHashMap;

import com.example.chess.api.MatchWatcher;
import com.example.chess.models.Player;

public class ChessMatchManager {

    private static ChessMatchManager instance;
    private ConcurrentHashMap<String, ChessMatch>   matches;
    private ConcurrentHashMap<String, MatchWatcher> watchers;

    private ChessMatchManager() {
        matches = new ConcurrentHashMap<>();
        watchers= new ConcurrentHashMap<>();
    }

    public static synchronized ChessMatchManager getInstance() {
        if (instance == null) {
            instance = new ChessMatchManager();
        }
        return instance;
    }

    public void addMatch(ChessMatch match) {

        MatchWatcher watcher = new MatchWatcher();
        
        matches .put(match.getWhite().toString(), match);
        matches .put(match.getBlack().toString(), match);
        watchers.put(match.getWhite().toString(), watcher);
        watchers.put(match.getBlack().toString(), watcher);
        
    }

    // métodos para buscar ou gerenciar partidas
    public ChessMatch getMatchFromPlayer(Player player){

        matches.keySet().stream().forEach(key -> System.out.println(key));
        System.out.println(player);

        return matches.get(player.toString());
    }

    // métodos para buscar ou gerenciar partidas
    public MatchWatcher getWatcherFromPlayer(Player player){

        watchers.keySet().stream().forEach(key -> System.out.println(key));
        System.out.println(player);

        return watchers.get(player.toString());
    }
}

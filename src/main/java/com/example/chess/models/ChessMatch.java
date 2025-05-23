package com.example.chess.models;

public class ChessMatch {

    Player white;
    Player black;
    String state;

    public ChessMatch(Player player1, Player player2){
        white = player1;
        black = player2;
    }

    public Player getBlack() {
        return black;
    }

    public Player getWhite() {
        return white;
    }
}

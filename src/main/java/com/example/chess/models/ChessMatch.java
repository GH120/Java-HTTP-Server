package com.example.chess.models;

import java.util.LinkedList;
import java.util.Queue;

public class ChessMatch {

    Player        white;
    Player        black;
    Queue<String> history;
    Piece[][]     board;


    public ChessMatch(Player player1, Player player2){
        white   = player1;
        black   = player2;
        history = new LinkedList<String>();
        board   = new Piece[8][8];
    }

    public Player getBlack() {
        return black;
    }

    public Player getWhite() {
        return white;
    }

    public Piece getPiece(Position position){
        return board[position.x][position.y];
    }

    public Piece[][] getBoard(){
        return board;
    }

    private void populateGameStart(){
        //Insere todas as peças do tabuleiro na partida
    }
}

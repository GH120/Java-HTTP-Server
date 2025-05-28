package com.example.chess.models;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChessMatch {
    
    Queue<String> history;
    Integer       turn;
    Piece[][]     board;

    Player        white;
    Player        black;
    Set<Piece>    blackPieces; //Colocar peças no player?
    Set<Piece>    whitePieces;

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

    public PieceColor getCurrentPlayer(){
        return turn % 2 == 0 ? PieceColor.WHITE : PieceColor.BLACK;
    }

    public PieceColor getOpponent(){
        return turn % 2 == 0 ? PieceColor.BLACK : PieceColor.WHITE;
    }

    public Set<Piece> getAllPieces(PieceColor color){
        return color == PieceColor.WHITE ? whitePieces : blackPieces;
    }

    public void registerMove(Move move){

        history.add(move.toString());
        
        turn++;
    }

    public static boolean withinBoard(Piece[][] board, Position position){

        Integer length = board.length;

        return position.x > 0 && position.y > 0 && position.x < length && position.y < length;
    }

    private void populateGameStart(){
        //Insere todas as peças do tabuleiro na partida
    }

}

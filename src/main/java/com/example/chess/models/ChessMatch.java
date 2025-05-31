package com.example.chess.models;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import com.example.chess.models.chesspieces.King;

//Classe primariamente de dados e acesso a dados
//Comportamento de jogada tratada em GameController
//Validação de regras feita em ChessRules
public class ChessMatch{
    
    private Stack<Move>   history;
    private Piece[][]     board;
    private Player        white;
    private Player        black;
    
    private HashMap<Piece, Integer> moveCount; //Responsabilidade separada, mover para outra classe?

    public ChessMatch(Player player1, Player player2){
        white     = player1;
        black     = player2;
        history   = new Stack<Move>();
        board     = new Piece[8][8];
        moveCount = new HashMap<>();
    }

    /** Não valida jogada, apenas joga ela assumindo que foi validada */
    public void play(Piece piece, Move move){

        kill(getPiece(move.destination)); 

        piece.position = move.destination;

        board[move.destination.x][move.destination.y] = piece;

        treatSideEffects(piece, move);

        history.add(move);

    }

    public void placePiece(Piece piece, Position position){
        piece.position = position;

        board[position.x][position.y] = piece;
        
        getAllPieces(piece.color).add(piece);
    }

    public void kill(Piece attackedPiece){

        if(attackedPiece == null) return;

        getAllPieces(attackedPiece.getColor()).remove(attackedPiece);

        board[attackedPiece.position.x][attackedPiece.position.y] = null;

    }

    //Getters
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

    public int getTurn(){
        return history.size();
    }

    public PieceColor getCurrentColor(){
        return getTurn() % 2 == 0 ? PieceColor.WHITE : PieceColor.BLACK;
    }

    public PieceColor getOpponentColor(){
        return getTurn() % 2 == 0 ? PieceColor.BLACK : PieceColor.WHITE;
    }

    public Set<Piece> getAllPieces(PieceColor color){
        return color == PieceColor.WHITE ? white.pieces : black.pieces;
    }

    public Move getLastMove(){
        return history.peek();
    }

    //**Esquece última jogada do histórico e decrementa o número de movimentos da peça afetada */
    public ChessMatch forgetMove(){

        //Decrementa o número de jogadas da peça movida
        Piece piece = getPiece(getLastMove().origin);

        moveCount.compute(piece, (p, i) -> i - 1);

        history.pop();

        return this;
    }


    public King findKing(PieceColor color){

        return (King) getAllPieces(color).stream()
                                         .filter(p -> p instanceof King)
                                         .findFirst()
                                         .orElse(null);
    }

    public static boolean withinBoard(Piece[][] board, Position position){

        Integer length = board.length;

        return position.x >= 0 && position.y >= 0 && position.x < length && position.y < length;
    }

    public boolean hasMoved(Piece piece){
        return moveCount.get(piece) > 0;
    }

    /**Lida com os efeitos colaterais de jogadas como o en passant */
    private void treatSideEffects(Piece piece, Move move){

        switch(move.event){
            case EN_PASSANT -> {
                
                Piece victim = move.event.target;

                kill(victim);
            }
            default -> {

            }
        }
    }

    private void populateGameStart(){
        //Insere todas as peças do tabuleiro na partida
    }

}

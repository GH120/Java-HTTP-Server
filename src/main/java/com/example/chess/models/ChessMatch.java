package com.example.chess.models;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import com.example.chess.models.chesspieces.Bishop;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Knight;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;
import com.example.chess.models.chesspieces.Rook;

//Classe primariamente de dados e acesso a dados
//Comportamento de jogada tratada em GameController
//Validação de regras feita em ChessRules
public class ChessMatch{
    
    private Stack<Move>   history;
    private Piece[][]     board;
    private Player        white;
    private Player        black;
    private Stack<Piece>  casualties;
    
    private HashMap<Piece, Integer> moveCount; //Responsabilidade separada, mover para outra classe?

    public ChessMatch(Player player1, Player player2){
        white      = player1;
        black      = player2;
        history    = new Stack<Move>();
        board      = new Piece[8][8];
        moveCount  = new HashMap<>();
        casualties = new Stack<>();
    }

    //////////////////////////////////////
    // -- Métodos que Alteram Estado -- //
    //////////////////////////////////////
    

    /** Não valida jogada, apenas joga ela assumindo que foi validada 
     *  Se houver peça no quadrado do destino, mata ela
    */
    public void play(Piece piece, Move move){

        kill(getPiece(move.destination)); 

        piece.position = move.destination;

        board[move.destination.x][move.destination.y] = piece;

        treatSideEffects(piece, move);

        moveCount.compute(piece, (p,i) -> i + 1);

        history.add(move);

    }

    public void insertPiece(Piece piece, Position position){
        piece.position = position;

        board[position.x][position.y] = piece;
        
        getAllPieces(piece.color).add(piece);
    }

    public void kill(Piece attackedPiece){

        if(attackedPiece == null) return;

        getAllPieces(attackedPiece.getColor()).remove(attackedPiece);

        board[attackedPiece.position.x][attackedPiece.position.y] = null;

        casualties.add(attackedPiece);

    }

    /**Esquece última jogada do histórico, 
     * decrementa o número de movimentos da peça que se moveu, 
     * revive peça morta no último turno */
    public ChessMatch revertLastMove(){

        Move lastMove = getLastMove();

        Piece piece = getPiece(lastMove.origin);

        Move moveBack = new Move(lastMove.destination, lastMove.origin);

        play(piece, moveBack);

        revertSideEffects(piece, moveBack);

        insertPiece(casualties.pop(), lastMove.destination);

        //Remove as duas jogadas (ida e volta) da peça movida
        moveCount.compute(piece, (p, i) -> i - 2);
        history.pop();
        history.pop();

        return this;
    }

    public void choosePromotion(Pawn.Promotion promotion){

        Pawn pawn = (Pawn) getPiece(getLastMove().destination);

        kill(pawn);
        
        Piece promotedPiece = null;

        switch(promotion){
            case KNIGHT -> promotedPiece = new Knight(pawn.position, pawn.color);
            case QUEEN  -> promotedPiece = new Queen (pawn.position, pawn.color);
            case ROOK   -> promotedPiece = new Rook  (pawn.position, pawn.color);
            case BISHOP -> promotedPiece = new Bishop(pawn.position, pawn.color);
        }

        insertPiece(promotedPiece, pawn.position);
    }

    ///////////////////////////////////////
    // -- Métodos que Retornam Estado -- //
    ///////////////////////////////////////

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

        if(history.empty()) return null;

        return history.peek();
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

    ////////////////////////////
    // -- Métodos Privados -- //
    ////////////////////////////

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

    /** Tratar casos de En-passant, Castle*/
    private void revertSideEffects(Piece piece, Move move){
        
        switch(move.event){
            case EN_PASSANT -> {
                
                //Acabou sendo desnecessário, pois o forgetMove já revive a última peça morta
                // Piece victim = move.event.target;

                // insertPiece(victim, move.origin);
            }
            default -> {

            }
        }
        
    }

    private void populateGameStart(){
        //Insere todas as peças do tabuleiro na partida
    }

}

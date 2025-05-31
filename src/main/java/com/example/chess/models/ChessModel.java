package com.example.chess.models;

import java.util.HashMap;
import java.util.HashSet;
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
public class ChessModel{
    
    private Stack<Move>   history;
    private Piece[][]     board;
    private Set<Piece>    whitePieces;
    private Set<Piece>    blackPieces;
    private Stack<Piece>  casualties;
    
    private HashMap<Piece, Integer> moveCount; //Responsabilidade separada, mover para outra classe?

    public ChessModel(){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();

        populateGameStart(); //Tabuleiro de xadrez padrão, extrair depois método separado em uma classe Factory
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
    public ChessModel revertLastMove(){

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
        return color == PieceColor.WHITE ? whitePieces : blackPieces;
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

        // Peças brancas (linha 0 e 1)
        insertPiece(new Rook(new Position(0, 0), PieceColor.WHITE));
        insertPiece(new Knight(new Position(0, 1), PieceColor.WHITE));
        insertPiece(new Bishop(new Position(0, 2), PieceColor.WHITE));
        insertPiece(new Queen(new Position(0, 3), PieceColor.WHITE));
        insertPiece(new King(new Position(0, 4), PieceColor.WHITE));
        insertPiece(new Bishop(new Position(0, 5), PieceColor.WHITE));
        insertPiece(new Knight(new Position(0, 6), PieceColor.WHITE));
        insertPiece(new Rook(new Position(0, 7), PieceColor.WHITE));
        
        // Peões brancos (linha 1)
        for (int col = 0; col < 8; col++) {
            insertPiece(new Pawn(new Position(1, col), PieceColor.WHITE));
        }

        // Peças pretas (linha 7 e 6)
        insertPiece(new Rook(new Position(7, 0), PieceColor.BLACK));
        insertPiece(new Knight(new Position(7, 1), PieceColor.BLACK));
        insertPiece(new Bishop(new Position(7, 2), PieceColor.BLACK));
        insertPiece(new Queen(new Position(7, 3), PieceColor.BLACK));
        insertPiece(new King(new Position(7, 4), PieceColor.BLACK));
        insertPiece(new Bishop(new Position(7, 5), PieceColor.BLACK));
        insertPiece(new Knight(new Position(7, 6), PieceColor.BLACK));
        insertPiece(new Rook(new Position(7, 7), PieceColor.BLACK));
        
        // Peões pretos (linha 6)
        for (int col = 0; col < 8; col++) {
            insertPiece(new Pawn(new Position(6, col), PieceColor.BLACK));
        }
    }

    // Método auxiliar para simplificar a inserção
    private void insertPiece(Piece piece) {
        insertPiece(piece, piece.position);
    }
}

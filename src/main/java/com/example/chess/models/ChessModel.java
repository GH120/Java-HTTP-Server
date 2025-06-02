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
    
    private Piece[][]       board;
    private Set<Piece>      whitePieces;
    private Set<Piece>      blackPieces;
    private Stack<Move>     history;

    //Verifica as peças abatidas nas jogadas
    private Stack<Piece>    casualties;
    private Stack<Boolean>  attackMove;
    
    private HashMap<Piece, Integer> moveCount; //Responsabilidade separada, mover para outra classe?

    public ChessModel(){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();
        attackMove  = new Stack<>();

        populateGameStart(); //Tabuleiro de xadrez padrão, extrair depois método separado em uma classe Factory

        whitePieces.forEach(p -> moveCount.put(p, 0));
        blackPieces.forEach(p -> moveCount.put(p, 0));
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

        board[move.origin.x][move.origin.y] = null;

        treatSideEffects(piece, move);

        moveCount.compute(piece, (p,i) -> i + 1);

        history.add(move);

    }

    public void insertPiece(Piece piece, Position position){
        piece.position = position;

        board[position.x][position.y] = piece;
        
        getAllPieces(piece.color).add(piece);
    }

    // Método auxiliar para simplificar a inserção
    public void insertPiece(Piece piece) {
        insertPiece(piece, piece.position);
    }

    public void kill(Piece attackedPiece){

        if(attackedPiece == null){
            attackMove.push(false);
            
            return;
        }

        getAllPieces(attackedPiece.getColor()).remove(attackedPiece);

        board[attackedPiece.position.x][attackedPiece.position.y] = null;

        casualties.add(attackedPiece);

        attackMove.push(true);
    }

    /**Esquece última jogada do histórico, 
     * decrementa o número de movimentos da peça que se moveu, 
     * revive peça morta no último turno */
    public ChessModel revertLastMove(){

        Move lastMove = getLastMove();

        Piece piece = getPiece(lastMove.destination);

        Move moveBack = new Move(lastMove.destination, lastMove.origin);

        play(piece, moveBack);

        revertSideEffects(piece, moveBack);

        //ele tem que saber se na última jogada houve morte ou não
        if(attackMove.pop()){
            insertPiece(casualties.pop(), lastMove.destination); 
        }

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

    public PlayerColor getCurrentColor(){
        return getTurn() % 2 == 0 ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    public PlayerColor getOpponentColor(){
        return getTurn() % 2 == 0 ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    public Set<Piece> getAllPieces(PlayerColor color){
        return color == PlayerColor.WHITE ? whitePieces : blackPieces;
    }

    public Move getLastMove(){

        if(history.empty()) return null;

        return history.peek();
    }

    public Stack<Piece> getCasualties(){
        return this.casualties;
    }

    public King findKing(PlayerColor color){

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
            case CASTLING -> {

                Piece rook = move.event.target;

                boolean isKingside = rook.position.x == 7;

                Position destination = new Position(isKingside? 5 : 3, rook.position.y);

                piece.position = destination;

                board[destination.x][destination.y] = piece;
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
            case CASTLING -> {

                Piece rook = move.event.target;

                boolean isKingside = rook.position.x == 5;

                Position destination = new Position(isKingside? 7 : 0, rook.position.y);

                piece.position = destination;

                board[destination.x][destination.y] = piece;
            }
            default -> {

            }
        }
        
    }

   private void populateGameStart() {
        // Peças brancas (linha 0 - peças principais)
        insertPiece(new Rook(new Position(0, 0), PlayerColor.WHITE));       // Torre a1
        insertPiece(new Knight(new Position(1, 0), PlayerColor.WHITE));     // Cavalo b1
        insertPiece(new Bishop(new Position(2, 0), PlayerColor.WHITE));     // Bispo c1
        insertPiece(new Queen(new Position(3, 0), PlayerColor.WHITE));      // Rainha d1
        insertPiece(new King(new Position(4, 0), PlayerColor.WHITE));       // Rei e1
        insertPiece(new Bishop(new Position(5, 0), PlayerColor.WHITE));     // Bispo f1
        insertPiece(new Knight(new Position(6, 0), PlayerColor.WHITE));     // Cavalo g1
        insertPiece(new Rook(new Position(7, 0), PlayerColor.WHITE));       // Torre h1

        // Peões brancos (linha 1)
        for (int col = 0; col < 8; col++) {
            insertPiece(new Pawn(new Position(col, 1), PlayerColor.WHITE)); // a2-h2
        }

        // Peças pretas (linha 7 - peças principais)
        insertPiece(new Rook(new Position(0, 7), PlayerColor.BLACK));       // Torre a8
        insertPiece(new Knight(new Position(1, 7), PlayerColor.BLACK));     // Cavalo b8
        insertPiece(new Bishop(new Position(2, 7), PlayerColor.BLACK));     // Bispo c8
        insertPiece(new Queen(new Position(3, 7), PlayerColor.BLACK));      // Rainha d8
        insertPiece(new King(new Position(4, 7), PlayerColor.BLACK));       // Rei e8
        insertPiece(new Bishop(new Position(5, 7), PlayerColor.BLACK));     // Bispo f8
        insertPiece(new Knight(new Position(6, 7), PlayerColor.BLACK));     // Cavalo g8
        insertPiece(new Rook(new Position(7, 7), PlayerColor.BLACK));       // Torre h8

        // Peões pretos (linha 6)
        for (int col = 0; col < 8; col++) {
            insertPiece(new Pawn(new Position(col, 6), PlayerColor.BLACK)); // a7-h7
        }
    }

}

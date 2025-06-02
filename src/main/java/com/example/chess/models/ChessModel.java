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
    
    //Estado do jogo
    private Piece[][]       board;
    private Set<Piece>      whitePieces;
    private Set<Piece>      blackPieces;
    private Stack<Move>     history;

    //Verifica as peças abatidas nas jogadas
    private Stack<Piece>    casualties;
    private Stack<Boolean>  attackMove;
    
    //Responsabilidade separada, mover para outra classe? Serve para saber se a peça se moveu
    private HashMap<Piece, Integer> moveCount;

    public ChessModel(StartingPieces pieces){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();
        attackMove  = new Stack<>();

        pieces.populateBoard(this); //Tabuleiro de xadrez padrão, extrair depois método separado em uma classe Factory
    }

    public ChessModel(){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();
        attackMove  = new Stack<>();
    }

    //////////////////////////////////////
    // -- Métodos que Alteram Estado -- //
    //////////////////////////////////////
    

    /** Não valida jogada, apenas joga ela assumindo que foi validada 
     *  Se houver peça no quadrado do destino, mata ela
     *  efeitos colaterais: adiciona as pilhas attackMove, casualties e history
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

        moveCount.put(piece, 0);
    }

    // Método auxiliar para simplificar a inserção
    public void insertPiece(Piece piece) {
        insertPiece(piece, piece.position);
    }

    /**Elimina uma peça do tabuleiro e de peças ativas se estiver viva
     * Nesse caso, adiciona a pilha casualties e true na attackMove
     */
    public void kill(Piece attackedPiece){

        if(attackedPiece == null){
            attackMove.push(false);
            
            return;
        }

        getAllPieces(attackedPiece.getColor()).remove(attackedPiece);

        board[attackedPiece.position.x][attackedPiece.position.y] = null;

        //Efeito colateral, propício a bugs
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
        

        //Remove efeito colateral desse último play (pilha de casualties não é alterada)
        history.pop();
        attackMove.pop();

        //Remove as duas jogadas (ida e volta) da peça movida
        moveCount.compute(piece, (p, i) -> i - 2);

        revertSideEffects(piece, moveBack);

        

        //Esquece última jogada e suas consequências
        history.pop();
        
        //ele tem que saber se na última jogada houve morte ou não
        //Se houver, reinsere peça
        if(attackMove.pop()){
            insertPiece(casualties.pop(), lastMove.destination); 
        }

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

    /**Cachear depois, busca bruta extremamente ineficiente */
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

}

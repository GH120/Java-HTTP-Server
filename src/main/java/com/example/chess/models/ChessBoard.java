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

/** Estado do tabuleiro de Xadrez;
 * Responsabilidades: 1. Armazenar informações sobre estado das peças, tabuleiro e histórico;
 * 2. Transicionar entre um estado válido para outro válido por meio dos métodos play e reverse;
 * 3. Não implementa nem validação nem tratamento de erros
*/
public class ChessBoard{

    //TODO: Ver como usar o mockito
    //OBS: Modificação não é thread safe, mas sincronização na partida ChessMatch garante acesso único
    
    //Estado do jogo
    private Piece[][]       board;
    private Set<Piece>      whitePieces;
    private Set<Piece>      blackPieces;
    private Stack<Move>     history;
    private Stack<Piece>    casualties; //Verifica as peças abatidas nas jogadas
    private HashMap<Piece, Integer> moveCount; 

    //Cache
    private HashMap<PlayerColor, King> kings;

    public ChessBoard(StartingPieces pieces){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();
        kings       = new HashMap<>();

        pieces.populateBoard(this); //Tabuleiro de xadrez padrão, extrair depois método separado em uma classe Factory
    }

    public ChessBoard(){
        history     = new Stack<Move>();
        board       = new Piece[8][8];
        moveCount   = new HashMap<>();
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        casualties  = new Stack<>();
        kings       = new HashMap<>();
    }

    //////////////////////////////////////
    // -- Métodos que Alteram Estado -- //
    //////////////////////////////////////
    

    /** Não valida jogada, apenas joga ela assumindo que foi validada 
     *  Se houver peça no quadrado do destino, mata ela
     *  efeitos colaterais: adiciona as pilhas attackMove, casualties e history
    */
    public void applyMove(Piece piece, Move move){

        capture(getPiece(move.destination)); 

        piece.position = move.destination; //Informação interna da posição

        board[move.destination.x][move.destination.y] = piece; //Onde ele vai 

        board[move.origin.x][move.origin.y] = null; //Onde ele veio fica nulo

        treatSideEffects(piece, move);

        //Adicionar eles abaixo no componente interno 'histórico', responsável por reverter jogadas
        moveCount.compute(piece, (p,i) -> i + 1); //Incrementa número de jogadas dessa peça

        history.add(move);

    }

    public void placePiece(Piece piece, Position position){
        piece.position = position;

        board[position.x][position.y] = piece;
        
        getAllPieces(piece.color).add(piece);

        moveCount.put(piece, 0);
    }

    // Método auxiliar para simplificar a inserção
    public void placePiece(Piece piece) {
        placePiece(piece, piece.position);
    }

    /**Elimina uma peça do tabuleiro e de peças ativas se estiver viva
     * Adiciona a pilha de casualties (até mesmo se for nula)
     */
    public void capture(Piece attackedPiece){

        //Adiciona a pilha de casualties, mesmo se for nulo (não tinha peça atacada)
        //Propício a bugs, efeito colateral indesejado
        casualties.add(attackedPiece);

        if(attackedPiece == null) return;

        getAllPieces(attackedPiece.getColor()).remove(attackedPiece);

        board[attackedPiece.position.x][attackedPiece.position.y] = null;

    }

    //TODO: Ponto fraco - não consegue reverter promoções de xadrez
    //Para verificações de xeque, isso não importa
    //Para IAs ou outras classes que queiram usar desse modelo, talvez seja

    /**Esquece última jogada do histórico, 
     * decrementa o número de movimentos da peça que se moveu, 
     * revive peça morta no último turno */
    public ChessBoard revertMove(){

        Move lastMove = getLastMove();

        Piece piece  = getPiece(lastMove.destination);

        Move moveBack = new Move(lastMove.destination, lastMove.origin);

        applyMove(piece, moveBack); //Move de volta a peça

        //Remove as duas jogadas (ida e volta) da peça movida
        history.pop();    //Remove movimento desse último play
        casualties.pop(); //Remove ataque    desse último play
        history.pop();    //Remove última jogada 
        moveCount.compute(piece, (p, i) -> i - 2);

        //Se houve vitima na última jogada, reinsere no tabuleiro
        Piece victim = casualties.pop(); //Muito propício a bugs
        
        if(victim != null) placePiece(victim, lastMove.destination);

        revertSideEffects(piece, lastMove);

        return this;
    }

    public void choosePromotion(Pawn.Promotion promotion){

        Pawn pawn = (Pawn) getPiece(getLastMove().destination);

        capture(pawn);
        
        Piece promotedPiece = null;

        switch(promotion){
            case KNIGHT -> promotedPiece = new Knight(pawn.position, pawn.color);
            case QUEEN  -> promotedPiece = new Queen (pawn.position, pawn.color);
            case ROOK   -> promotedPiece = new Rook  (pawn.position, pawn.color);
            case BISHOP -> promotedPiece = new Bishop(pawn.position, pawn.color);
        }

        placePiece(promotedPiece, pawn.position);
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

    //Fazer filtro para retornar apenas as casualties ignorando valores nulos
    public Stack<Piece> getCasualties(){
        return this.casualties; //Passa a ser responsabilidade do histórico
    }

    /**Procura Rei da cor solicitada e cacheia ele, retornando caso seja chamada de novo */
    public King findKing(PlayerColor color){

        return kings.compute(color, (c,k) -> ((King) getAllPieces(color)
                                                    .stream()
                                                    .filter(p -> p instanceof King)
                                                    .findFirst()
                                                    .orElse(null)));
    }

    public static boolean withinBoard(Piece[][] board, Position position){

        Integer length = board.length;

        return position.x >= 0 && position.y >= 0 && position.x < length && position.y < length;
    }

    public boolean hasMoved(Piece piece){
        return moveCount.get(piece) > 0; //Passa a ser responsabilidade do histórico
    }

    ////////////////////////////
    // -- Métodos Privados -- //
    ////////////////////////////

    /**Lida com os efeitos colaterais de jogadas como o en passant */
    private void treatSideEffects(Piece piece, Move move){

        switch(move.event){
            case EN_PASSANT -> {
                
                Piece victim = move.event.target;

                capture(victim);
            }
            case CASTLING -> {

                Piece rook = move.event.target;

                boolean isKingside = rook.position.x == 7;

                Position destination = new Position(isKingside? 5 : 3, rook.position.y);

                board[rook.position.x][rook.position.y] = null;
                board[destination.x][destination.y]     = rook;
                
                rook.position = destination;
            }
            default -> {

            }
        }
    }

    //TODO: Tratar caso de peão promovido não ser revertido
    //Isso não afeta a verificação de xeque que usa o revert move, 
    //Mas isso pode afetar outros componentes como IA que tentem reverter esse estado
    
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

                board[rook.position.x][rook.position.y] = null;
                board[destination.x][destination.y]     = rook;
                
                rook.position = destination;
            }
            
            //Já é tratado no revert move que coloca o peão de volta, falta só tirar a peça promovida das peças do jogador
            case PROMOTION -> {

                // //ANÁLISE: NÃO FAZ NADA PARA JOGADAS INCOMPLETAS ONDE O JOGADOR NÃO ESCOLHEU A PROMOÇÃO
                // if(piece instanceof Pawn) return;

                // capture(piece); //Captura a peça promovida 
                // casualties.pop(); //Se esquece dela

                // Piece pawn = casualties.pop(); //Consegue de volta o peão morto

                // placePiece(piece);

                //
                if(!(piece instanceof Pawn)) getAllPieces(piece.color).remove(piece);
                
            }
            default -> {

            }
        }
        
    }

}

package com.example.chess.controlers;

import java.util.HashMap;
import java.util.List;

import com.example.chess.models.ChessModel;
import com.example.chess.models.ChessRules;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.models.chesspieces.Bishop;
import com.example.chess.models.chesspieces.Knight;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Queen;
import com.example.chess.models.chesspieces.Rook;

//Transformar ele numa thread?
//Controlaria as ações do usuário, como escolher jogadas ou sair da partida
//Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado
public class ChessMatch {

    private Player     white;
    private Player     black;
    private ChessRules chessRules;
    private ChessModel chessModel;
    private GameState  state;

    private HashMap<Position, List<Move>> moveCache;
    
    private enum GameState {
        NORMAL, 
        CHECK, 
        CHECKMATE, 
        DRAW, 
        PROMOTION, //Para partida enquanto usuário não escolher a promoção
        EXITED
    }

    public ChessMatch(Player player, Player opponent) {
        moveCache  = new HashMap<>();
        chessRules = new ChessRules();
        chessModel = new ChessModel();
        white = player;
        black = opponent;
    }

    public void choosePromotion(Pawn.Promotion promotion) throws NoPromotionEvent{

        if(state != GameState.PROMOTION) 
            throw new NoPromotionEvent();

        chessModel.choosePromotion(promotion);

        updateGameState();
    }

    //Controls
    public void playMove(Player player, Move move) throws InvalidMove, NotPlayerTurn, PendingPromotion{


        if(state == GameState.PROMOTION)
            throw new PendingPromotion();

        List<Move> moves = seePossibleMoves(move.origin);

        if(!moves.contains(move)) 
            throw new InvalidMove();

        Piece piece = chessModel.getPiece(move.origin);

        if(piece.color == chessModel.getCurrentColor())
            throw new NotPlayerTurn();

        chessModel.play(piece, move);

        updateGameState();
        
        handleResponses(move);

        moveCache.clear();
    }

    public List<Move> seePossibleMoves(Position position){

        return moveCache.computeIfAbsent(position, pos ->{
        
            Piece piece = chessModel.getPiece(position);

            List<Move> defaultMoves = piece.defaultMoves(chessModel.getBoard());

            List<Move> allowedMoves = this.chessRules.validateMoves(chessModel, piece, defaultMoves);

            return allowedMoves;
        });
    }

    /** Responde com base no evento do movimento */
    private void handleResponses(Move move){

        switch(move.event){
            case PROMOTION -> {
                state = GameState.PROMOTION;
                //Colocar resposta especial aqui
            }
            default ->{
                //Resposta padrão
            }
        }
    }

    private void updateGameState(){

        if(chessRules.isInCheckMate(chessModel, chessModel.getCurrentColor())){
            state = GameState.CHECKMATE;
        }
        else if(chessRules.isInCheck(chessModel, chessModel.getCurrentColor())){
            state = GameState.CHECK;
        }
        else if(chessRules.isDraw(chessModel, chessModel.getCurrentColor())){
            state = GameState.DRAW;
        }

        state = GameState.NORMAL;
    }

    private void sendResponse(){
        
    }

    public Player getBlack() {
        return black;
    }

    public Player getWhite() {
        return white;
    }

    private class InvalidMove extends Exception{

        InvalidMove(){
            super("Jogada inválida");
        }
    }

    private class NotPlayerTurn extends Exception{

    }

    private class PendingPromotion extends Exception{

    }

    private class NoPromotionEvent extends Exception{

    }

}
package com.example.chess.controlers;

import java.util.HashMap;
import java.util.List;

import com.example.chess.models.ChessMatch;
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
public class GameController {

    //Controlaria as ações do usuário, como escolher jogadas ou sair da partida
    //Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado
    private static GameController instance;

    private HashMap<Position, List<Move>> moveCache;
    private ChessRules rules;
    private GameState  state;

    private enum GameState {
        NORMAL, 
        CHECK, 
        CHECKMATE, 
        DRAW, 
        PROMOTION, //Para partida enquanto usuário não escolher a promoção
        EXITED
    }

    private GameController() {
        moveCache = new HashMap<>();
        rules = new ChessRules();
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public void choosePromotion(ChessMatch match, Pawn.Promotion promotion) throws NoPromotionEvent{

        if(state != GameState.PROMOTION) 
            throw new NoPromotionEvent();

        match.choosePromotion(promotion);

        updateGameState(match);
    }

    //Controls
    public void playMove(Player player, ChessMatch match, Move move) throws InvalidMove, NotPlayerTurn, PendingPromotion{


        if(state == GameState.PROMOTION)
            throw new PendingPromotion();

        List<Move> moves = seePossibleMoves(match, move.origin);

        if(!moves.contains(move)) 
            throw new InvalidMove();

        Piece piece = match.getPiece(move.origin);

        if(piece.color == match.getCurrentColor())
            throw new NotPlayerTurn();

        match.play(piece, move);

        handleResponses(move);

        moveCache.clear();
    }

    public List<Move> seePossibleMoves(ChessMatch match, Position position){

        return moveCache.computeIfAbsent(position, pos ->{
        
            Piece piece = match.getPiece(position);

            List<Move> defaultMoves = piece.defaultMoves(match.getBoard());

            List<Move> allowedMoves = this.rules.validateMoves(match, piece, defaultMoves);

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

    private void updateGameState(ChessMatch match){

        if(rules.isInCheckMate(match, match.getCurrentColor())){
            state = GameState.CHECKMATE;
        }
        else if(rules.isInCheck(match, match.getCurrentColor())){
            state = GameState.CHECK;
        }
        else if(rules.isDraw(match, match.getCurrentColor())){
            state = GameState.DRAW;
        }

        state = GameState.NORMAL;
    }

    private void sendResponse(){
        
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
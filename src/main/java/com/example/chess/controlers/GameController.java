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
        NORMAL, CHECK, CHECKMATE, DRAW, EXITED
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

    public void choosePromotion(ChessMatch match, Pawn pawn, Pawn.Promotion promotion){

        match.kill(pawn);
        
        Piece promotedPiece = null;

        switch(promotion){
            case KNIGHT -> promotedPiece = new Knight(pawn.position, pawn.color);
            case QUEEN  -> promotedPiece = new Queen (pawn.position, pawn.color);
            case ROOK   -> promotedPiece = new Rook  (pawn.position, pawn.color);
            case BISHOP -> promotedPiece = new Bishop(pawn.position, pawn.color);
        }

        match.insertPiece(promotedPiece, pawn.position);
    }

    //Controls
    public void playMove(Player player, ChessMatch match, Move move) throws InvalidMove, NotPlayerTurn{

        List<Move> moves = seePossibleMoves(match, move.origin);

        if(!moves.contains(move)) 
            throw new InvalidMove();

        Piece piece = match.getPiece(move.origin);

        if(piece.color == match.getCurrentColor())
            throw new NotPlayerTurn();

        match.play(piece, move);

        handleEvents(move);

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

    //TODO: Adicionar verificação de cheque

    private void handleEvents(Move move){

        switch(move.event){
            case CHECK -> {

            }
            case CHECKMATE -> {

            }
            default ->{

            }
        }
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

}
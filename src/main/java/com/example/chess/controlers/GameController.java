package com.example.chess.controlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.ChessRules;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.models.chesspieces.King;

//Transformar ele numa thread?
public class GameController {

    //Controlaria as ações do usuário, como escolher jogadas ou sair da partida
    //Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado
    private static GameController instance;

    private HashMap<Position, List<Move>> moveCache;
    private ChessRules rules;

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

    //Controls
    public void playMove(Player player, ChessMatch match, Move move) throws InvalidMoveException{

        List<Move> moves = seePossibleMoves(match, move.origin);

        if(!moves.contains(move)) throw new InvalidMoveException();

        Piece piece = match.getPiece(move.origin);

        piece.apply(match.getBoard(), move);

        handleEvents(move);
    }

    public List<Move> seePossibleMoves(ChessMatch match, Position position){

        return moveCache.computeIfAbsent(position, pos ->{
        
            Piece piece = match.getPiece(position);

            List<Move> defaultMoves = piece.allowedMoves(match.getBoard());

            List<Move> allowedMoves = this.rules.validateMoves(match, piece, defaultMoves);

            return allowedMoves;
        });
    }

    private void handleEvents(Move move){

        switch(move.event){
            case CHECK -> {

            }
            case CHECKMATE -> {

            }
        }
    }

    private void sendResponse(){
        
    }

    private class InvalidMoveException extends Exception{

        InvalidMoveException(){
            super("Jogada inválida");
        }
    }

}
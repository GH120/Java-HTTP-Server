package com.example.chess.controlers;

import java.util.LinkedList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Player;
import com.example.chess.models.Position;

public class GameController {

    //Controlaria as ações do usuário, como escolher jogadas ou sair da partida
    //Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado

    private static GameController instance;

    private GameController() {

    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    //Controls
    public void playMove(Player player, ChessMatch match, Move move){

    }

    public List<Move> seePossibleMoves(ChessMatch match, Position position){

        Piece piece = match.getPiece(position);

        return piece.allowedMoves(match.getBoard());
    }

    //Supõe que jogada já é válida 
    //Verifica se a jogada causa cheque no adversário
    public boolean causesCheckOnOpponent(ChessMatch match, Move move){

        Piece      piece = match.getPiece(move.origin);
        PieceColor color = match.getCurrentPlayer();

        List<Move> attackedTiles = piece.allowedMovesFrom(match.getBoard(), move.destination);
        
        for(Move attack : attackedTiles){
            
            Piece attackedPiece = match.getPiece(attack.destination);

            if(attackedPiece instanceof King && !attackedPiece.sameColor(color)){
                return true;
            }
        }

        return false;
    }

    //Retorna se jogada inválida pois deixa o rei exposto
    //Cuidado com peças que são devoradas e não devem ser mais consideradas
    public boolean causesCheckOnThemselves(ChessMatch match, Move move){

        Piece king = findKing(match, match.getCurrentPlayer());

        // for(Piece enemyPiece : match.adversary)
        // for(Move attack : enemyPiece.allowedMoves)
        // if attack hits king returns true

        return false;
    }

    //Retorna se a jogada causa um cheque mate, onde o rei não tem escape
    //Cuidado com peças que são devoradas e não devem ser mais consideradas
    public boolean checkMate(ChessMatch match, Move move){


        //if opponent moves causes check
        //if for every move king does, he still is in check

        return false;
    }

    public Piece findKing(ChessMatch match, PieceColor color){
        
        for(int i = 0; i < 8; i++){
            for(int j=0; j< 8; j++){
                Piece piece = match.getBoard()[i][j];
                
                if(piece == null) continue;

                if(piece instanceof King)
                    if(piece.sameColor(color))
                        return piece;
            }
        }

        return null;
    }

}


abstract class King extends Piece{

}
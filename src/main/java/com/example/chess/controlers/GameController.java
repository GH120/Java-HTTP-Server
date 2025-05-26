package com.example.chess.controlers;

import java.util.LinkedList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
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

}

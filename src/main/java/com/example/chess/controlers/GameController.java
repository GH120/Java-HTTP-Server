package com.example.chess.controlers;

import java.util.LinkedList;

import javax.swing.text.Position;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Move;
import com.example.chess.models.Player;

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

    public void seePossibleMoves(Player player, ChessMatch match, Position position){

    }

}

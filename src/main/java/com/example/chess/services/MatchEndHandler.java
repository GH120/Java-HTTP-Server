package com.example.chess.services;

import java.util.List;

import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.services.ChessMatch.GameState;

//Vai lidar com o termino do jogo, desalocar partida e registrar ela numa database
//Desenvolvimento Posterior, primeiro testar o MVP usando só o MatchEventDispacher
public class MatchEndHandler implements MatchObserver{

    private final ChessMatch match;

    public MatchEndHandler(ChessMatch match){
        this.match = match;
    }

    @Override
    public void onMoveExecuted(Move move, PlayerColor currentPlayer) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onMoveExecuted'");
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        // TODO Auto-generated method stub
        
        if(hasEnded(newState)){

            ChessMatchManager.getInstance().removeMatch(match);
            //Adicionaria também a database, mas isso poderia ser duma classe especializada de logging
            //Talvez eliminar todos os observadores, mas isso envolveria retornar o notifier e adicionar um método nele, melhor não
        }
    }

    @Override
    public void onPromotionRequired(Position pawnPosition) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onPromotionRequired'");
    }

    @Override
    public void onShowPossibleMoves(List<Move> moves) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onShowPossibleMoves'");
    }

    @Override
    public void onError(String message) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    private boolean hasEnded(GameState state){

        switch(state){
            case DRAW, TIMEOUT, CHECKMATE, EXITED -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

}

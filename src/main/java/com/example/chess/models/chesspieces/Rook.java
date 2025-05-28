package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Position;

public class Rook extends Piece{

    private final Direction[] attackDirections = {
                                                    Direction.NORTH, 
                                                    Direction.SOUTH,
                                                    Direction.EAST,
                                                    Direction.WEST
                                                };



    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            factorLine(board, moves, attackDirection);
        }

        return moves;
    }


    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Mesma função em queen e bishop, só muda o nome
    public void factorLine(Piece[][] board, List<Move> moves, Direction direction){
        
        Position tile      = position.neighbourTile(direction);
        Position lastTile  = position;
        Piece    neighbour = board[lastTile.x][lastTile.y];

        while(neighbour == null){

            //Saiu do tamanho do tabuleiro
            if(!ChessMatch.withinBoard(board, tile)) break;

            neighbour = board[tile.x][tile.y];

            //Só adiciona se for inimigo ou quadrante vazio, peças amigas não são adicionadas
            if(enemyPiece(neighbour) || neighbour == null){
                moves.add(new Move(lastTile, tile));
            }

            lastTile = tile;
        } 
    }
}

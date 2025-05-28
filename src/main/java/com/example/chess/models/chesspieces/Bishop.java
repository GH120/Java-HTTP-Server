package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Position;

public class Bishop extends Piece{

    private final Direction[] attackDirections = {
                                                    Direction.NORTHEAST, 
                                                    Direction.NORTHWEST,
                                                    Direction.SOUTHEAST,
                                                    Direction.SOUTHWEST
                                                };

    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            factorDiagonal(board, moves, attackDirection);
        }

        return moves;
    }

    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Mesma função em rook e queen, só muda o nome
    public void factorDiagonal(Piece[][] board, List<Move> moves, Direction direction){
        
        Integer  length    = board.length;
        
        Position tile      = position.neighbourTile(direction);
        Position lastTile  = position;
        Piece    neighbour = board[lastTile.x][lastTile.y];

        while(neighbour == null){

            //Saiu do tamanho do tabuleiro
            if(tile.x < 0 || tile.x >= length || tile.y < 0 || tile.y >= length) break;

            neighbour = board[tile.x][tile.y];

            //Só adiciona se for inimigo ou quadrante vazio, peças amigas não são adicionadas
            if(enemyPiece(neighbour) || neighbour == null){
                moves.add(new Move(lastTile, tile));
            }

            lastTile = tile;
        } 
    }
}

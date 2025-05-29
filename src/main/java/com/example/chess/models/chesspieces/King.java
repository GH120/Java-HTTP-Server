package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Position;

public class King extends Piece{

    private boolean hasMoved = false;

    private final Direction[] attackDirections = {
                                                    Direction.NORTHEAST, 
                                                    Direction.NORTHWEST,
                                                    Direction.SOUTHEAST,
                                                    Direction.SOUTHWEST,
                                                    Direction.NORTH, 
                                                    Direction.SOUTH,
                                                    Direction.EAST,
                                                    Direction.WEST
                                                };

    //Retorna os movimentos permitidos
    //Dúvida: realizar verificação de cheque aqui ou no tabuleiro?
    //TODO: decidir como vai funcionar a verificação de cheque
    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            
            Position tile      = position.neighbourTile(attackDirection);
            Piece    neighbour = board[tile.x][tile.y];

            //Saiu do tabuleiro, ignora
            if(!ChessMatch.withinBoard(board, tile)) continue;

            //Só adiciona se for inimigo ou quadrante vazio, peças amigas não são adicionadas
            if(enemyPiece(neighbour) || neighbour == null){
                moves.add(new Move(position, tile));
            }

        }

        return moves;
    }

    public void apply(Piece[][] board, Move move){
        super.apply(board, move);

        hasMoved = true;
    }

    public boolean hasMoved(){
        return hasMoved;
    }
}

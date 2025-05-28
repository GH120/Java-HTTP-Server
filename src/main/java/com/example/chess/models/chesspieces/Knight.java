package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Position;

public class Knight extends Piece{

    private final Direction[] attackDirections = {
                                                    Direction.NORTH, 
                                                    Direction.SOUTH,
                                                    Direction.EAST,
                                                    Direction.WEST
                                                };



    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            factorAttackDirection(board, moves, attackDirection);
        }

        return moves;
    }


    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Diferente da função em queen, bishop e rook
    public void factorAttackDirection(Piece[][] board, List<Move> moves, Direction direction){
        
        Integer  length    = board.length;
        
        Position leftHook      = position.neighbourTile(direction)
                                         .neighbourTile(direction)
                                         .neighbourTile(direction.perpendicular(false));

        Position rightHook     = position.neighbourTile(direction)
                                         .neighbourTile(direction)
                                         .neighbourTile(direction.perpendicular(false));

        //Falta levar em conta se a posição contém uma peça inimiga
        moves.add(new Move(position, rightHook));
        moves.add(new Move(position, leftHook));
    }
}

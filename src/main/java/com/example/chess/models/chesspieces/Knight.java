package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessBoard;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;

public class Knight extends Piece{

    private final Direction[] attackDirections = {
                                                    Direction.NORTH, 
                                                    Direction.SOUTH,
                                                    Direction.EAST,
                                                    Direction.WEST
                                                };

    public Knight(Position position, PlayerColor color){
        super(position, color);
    }


    public List<Move> defaultMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            addAttackDirection(board, moves, attackDirection);
        }

        return moves;
    }


    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Diferente da função em queen, bishop e rook
    private void addAttackDirection(Piece[][] board, List<Move> moves, Direction direction){

        //Movimento em L: dois na direção e um na perpendicular (sentido antihorário)
        Position leftHook      = position.moveTo(direction)
                                         .moveTo(direction)
                                         .moveTo(direction.rotate90Degrees(false));

        //Movimento em L: dois na direção e um na perpendicular (sentido horário)
        Position rightHook     = position.moveTo(direction)
                                         .moveTo(direction)
                                         .moveTo(direction.rotate90Degrees(true));


        //Dois ataques na direção escolhida, um pra direita e outro pra esquerda
        attackInL(board, moves, rightHook);
        attackInL(board, moves, leftHook);
    }

    private void attackInL(Piece[][] board, List<Move> moves, Position attackedTile){

        
        if(ChessBoard.withinBoard(board, attackedTile)){
   
            Piece piece = board[attackedTile.x][attackedTile.y];
            
            //Se for uma peça inimiga ou não tiver peça, movimento válido
            if(enemyPiece(piece) || piece == null){
                moves.add(new Move(position, attackedTile));
            }
        }
    }
}

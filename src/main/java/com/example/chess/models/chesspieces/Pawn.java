package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Position;

public class Pawn extends Piece{

    //TODO: verificar se movimentos estão withinBoard
    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<>();

        //Avança uma casa para frente
        factorMoveForward(board, moves);

        //Movimento de pular duas casas se ainda não se moveu
        factorSkipTwoTiles(board, moves);

        //Movimentos de ataque
        factorAttackedTiles(board, moves);

        return null;
    }

    private void factorMoveForward(Piece[][] board, List<Move> moves){

        //Avança uma casa para frente
        Position tile = position.neighbourTile(Direction.NORTH);

        Piece neighbour = board[tile.x][tile.y];

        if(neighbour == null) {

            moves.add(new Move(position, tile));
            
        }

    }

    private void factorSkipTwoTiles(Piece[][] board, List<Move> moves){

        Position tile = position.neighbourTile(Direction.NORTH);

        Piece neighbour = board[tile.x][tile.y];

        boolean pawnHasntMoved = position.y == 1;

        if(neighbour != null && pawnHasntMoved){

            Position secondTile = tile.neighbourTile(Direction.NORTH);

            neighbour = board[secondTile.x][secondTile.y];

            if(neighbour == null){

                moves.add(new Move(position, secondTile));
            }
        }
    }

    private void factorAttackedTiles(Piece[][] board, List<Move> moves){

        Position tile;
        Piece neighbour;

        //Ataque diagonal esquerda
        tile = position.neighbourTile(Direction.NORTHWEST);

        neighbour = board[tile.x][tile.y];

        if(neighbour != null && enemyPiece(neighbour)){
            moves.add(new Move(position, tile));
        }

        //Ataque diagonal direita
        tile = position.neighbourTile(Direction.NORTHEAST);

        neighbour = board[tile.x][tile.y];

        if(neighbour != null && enemyPiece(neighbour)){
            moves.add(new Move(position, tile));
        }
    }
}

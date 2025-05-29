package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PieceColor;
import com.example.chess.models.Position;

public class Pawn extends Piece{

    //TODO: verificar se movimentos estão withinBoard
    public List<Move> allowedMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<>();

        addSingleForwardMove(board, moves); //Avança uma casa para frente
        addDoubleForwardMove(board, moves); //Movimento de pular duas casas se ainda não se moveu
        addCaptureMoves(board, moves); //Movimentos de ataque

        return moves;
    }

    private void addSingleForwardMove(Piece[][] board, List<Move> moves){

        //Avança uma casa para frente
        Position tile = position.neighbourTile(getDirectionConsideringColor(Direction.NORTH));

        //Se posição está fora do tabuleiro, é inválida
        if(!ChessMatch.withinBoard(board, tile)) return;

        Piece neighbour = board[tile.x][tile.y];

        if(neighbour == null) {

            moves.add(new Move(position, tile));
            
        }

    }

    private void addDoubleForwardMove(Piece[][] board, List<Move> moves){

       if(hasMoved()) return;

       Position firstStep  = position .neighbourTile(getDirectionConsideringColor(Direction.NORTH));

       if(!ChessMatch.withinBoard(board, firstStep)) return;

       Position secondStep = firstStep.neighbourTile(getDirectionConsideringColor(Direction.NORTH));
       
       if(!ChessMatch.withinBoard(board, secondStep)) return;

       if(board[firstStep.x][firstStep.y] == null && board[secondStep.x][secondStep.y] == null){

            moves.add(new Move(position, secondStep));
       }
    }

    private void addCaptureMoves(Piece[][] board, List<Move> moves){
        addDiagonalCapture(board, moves, Direction.NORTHEAST);
        addDiagonalCapture(board, moves, Direction.NORTHWEST);
    }

    private void addDiagonalCapture(Piece[][] board, List<Move> moves, Direction direction){

        //Ataque diagonal direita
        Position tile = position.neighbourTile(getDirectionConsideringColor(direction));

        Piece neighbour = board[tile.x][tile.y];

        if(neighbour != null && enemyPiece(neighbour)){
            moves.add(new Move(position, tile));
        }


    }

    public Direction getDirectionConsideringColor(Direction direction){
        return (getColor() == PieceColor.WHITE)? direction : direction.invert();
    }

    //NOTE: depende da suposição que os peões que estão na segunda fileira nunca se moveram
    private boolean hasMoved(){
        return (getColor() == PieceColor.WHITE)? position.y == 1 : position.y == 7;
    }
}

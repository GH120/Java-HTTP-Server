package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessBoard;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;

public class Bishop extends Piece{

    private final Direction[] attackDirections = {
                                                    Direction.NORTHEAST, 
                                                    Direction.NORTHWEST,
                                                    Direction.SOUTHEAST,
                                                    Direction.SOUTHWEST
                                                };

    public Bishop(Position position, PlayerColor color){
        super(position, color);
    }

    public List<Move> defaultMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            factorDiagonal(board, moves, attackDirection);
        }

        return moves;
    }

    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Mesma função em rook e queen, só muda o nome
    private void factorDiagonal(Piece[][] board, List<Move> moves, Direction direction){
        
        Position tile = position.moveTo(direction);
    
        while(ChessBoard.withinBoard(board, tile)) {

            Piece piece = board[tile.x][tile.y];
            
            // Se a posição estiver vazia, adiciona o movimento
            if(piece == null) {
                moves.add(new Move(position, tile));
            }
            // Se tiver uma peça inimiga, adiciona o movimento e para
            else if(enemyPiece(piece)) {
                moves.add(new Move(position, tile));
                break;
            }
            // Se tiver uma peça aliada, apenas para
            else break;
            
            // Move para a próxima posição na diagonal
            tile = tile.moveTo(direction);
        }
    }
}

package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessBoard;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;

public class King extends Piece{

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

    public King(Position position, PlayerColor color){
        super(position, color);
    }

    //Retorna os movimentos permitidos
    //Dúvida: realizar verificação de cheque aqui ou no tabuleiro?
    //TODO: decidir como vai funcionar a verificação de cheque
    public List<Move> defaultMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<Move>();

        for(Direction attackDirection : attackDirections){
            
            Position tile      = position.moveTo(attackDirection);
            
            //Saiu do tabuleiro, ignora
            if(!ChessBoard.withinBoard(board, tile)) continue;
            
            Piece    neighbour = board[tile.x][tile.y];
            
            //Só adiciona se for inimigo ou quadrante vazio, peças amigas não são adicionadas
            if(enemyPiece(neighbour) || neighbour == null){
                moves.add(new Move(position, tile));
            }
        }

        return moves;
    }
}

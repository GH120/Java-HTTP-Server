package com.example.chess.models.chesspieces;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
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

    //Função auxiliar que calcula todos os movimentos permitidos em uma direção
    //Mesma função em rook e bishop, só muda o nome
    public void factorLinesAndDiagonals(Piece[][] board, List<Move> moves, Direction direction){
        
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

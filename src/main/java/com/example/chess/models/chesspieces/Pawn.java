package com.example.chess.models.chesspieces;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.example.chess.models.ChessBoard;
import com.example.chess.models.Direction;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.http.HttpRequest;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.chess.models.Move.Event;

public class Pawn extends Piece{

    public enum Promotion{ KNIGHT, QUEEN, BISHOP, ROOK }

    public Pawn(Position position, PlayerColor color){
        super(position, color);
    }

    //TODO: verificar se movimentos estão withinBoard
    public List<Move> defaultMoves(Piece[][] board){

        ArrayList<Move> moves = new ArrayList<>();

        addSingleForwardMove(board, moves); //Avança uma casa para frente
        addDoubleForwardMove(board, moves); //Movimento de pular duas casas se ainda não se moveu
        addCaptureMoves(board, moves); //Movimentos de ataque

        return moves;
    }

    private void addSingleForwardMove(Piece[][] board, List<Move> moves){

        //Avança uma casa para frente
        Position tile = position.moveTo(getDirection(Direction.NORTH));

        //Se posição está fora do tabuleiro, é inválida
        if(!ChessBoard.withinBoard(board, tile)) return;

        Piece neighbour = board[tile.x][tile.y];

        if(neighbour == null) {

            moves.add(new Move(position, tile).setEvent(Event.MOVEMENT));
            
        }

    }

    //Talvez mover esse daqui para ChessRules, onde pode usar o estado da partida para verificar se moveu ou não o peão
    private void addDoubleForwardMove(Piece[][] board, List<Move> moves){

       if(hasMoved()) return;

       Position firstStep  = position .moveTo(getDirection(Direction.NORTH));

       if(!ChessBoard.withinBoard(board, firstStep)) return;

       Position secondStep = firstStep.moveTo(getDirection(Direction.NORTH));
       
       if(!ChessBoard.withinBoard(board, secondStep)) return;

       if(board[firstStep.x][firstStep.y] == null && board[secondStep.x][secondStep.y] == null){

            Move move = new Move(position, secondStep);

            moves.add(move.setEvent(Event.TWOTILESKIP));
       }
    }

    private void addCaptureMoves(Piece[][] board, List<Move> moves){
        addDiagonalCapture(board, moves, Direction.NORTHEAST);
        addDiagonalCapture(board, moves, Direction.NORTHWEST); //Poderia criar array attack directions com essas direções e condicionar a cor do peão
    }

    private void addDiagonalCapture(Piece[][] board, List<Move> moves, Direction direction){

        //Ataque diagonal direita
        Position tile = position.moveTo(getDirection(direction));

        if(!ChessBoard.withinBoard(board, tile)) return;

        Piece neighbour = board[tile.x][tile.y];

        if(neighbour != null && enemyPiece(neighbour)){
            moves.add(new Move(position, tile));
        }


    }

    /**Corrige a direção dependendo da cor (peças pretas tem direção invertida, norte vira sul) */
    public Direction getDirection(Direction direction){
        return (getColor() == PlayerColor.BLACK)? direction : direction.invert();
    }

    //NOTE: depende da suposição que os peões que estão na segunda fileira nunca se moveram
    private boolean hasMoved(){
        return !((getColor() == PlayerColor.BLACK)? position.y == 1 : position.y == 6);
    }

    //Criar uma fábrica para isso
    public static Promotion PromotionFromRequest(HttpRequest request) throws JsonProcessingException, IOException{

            JsonNode info = Json.parse(new String(request.getBody(), StandardCharsets.US_ASCII));

            return Pawn.Promotion.valueOf(info.get("promotion").asText());
    }
}

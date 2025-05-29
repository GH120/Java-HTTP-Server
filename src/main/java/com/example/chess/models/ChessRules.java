package com.example.chess.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

public class ChessRules {

    MoveSimulator simulator = new MoveSimulator();

    // Valida movimentos padrão e adiciona especiais (en passant, roque)
    public List<Move> validateMoves(ChessMatch match, Piece piece, List<Move> rawMoves) {
        
        List<Move> validMoves = new ArrayList<>();

        for (Move move : rawMoves) {

            if (wouldCauseSelfCheck(match, piece, move)) continue;
                
            validMoves.add(move);
        }

        addSpecialMoves(match, piece, validMoves);
        
        return validMoves;
    }

    // -- Regras Especiais -- //
    private void addSpecialMoves(ChessMatch match, Piece piece, List<Move> moves) {
        // if (piece instanceof King) addCastlingMoves(match,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(match, (Pawn) piece, moves);
    }

    // // Roque (pequeno e grande)
    // private void addCastlingMoves(ChessMatch match, King king, List<Move> moves) {
    //     if (king.hasMoved() || isInCheck(match, king.getColor())) return;

    //     Piece[][] board = match.getBoard();
    //     int row = king.position.x;

    //     // Roque pequeno (torre direita)
    //     if (canCastle(match, board, row, 7)) {
    //         moves.add(new Move(king.position, new Position(row, 6)));
    //     }

    //     // Roque grande (torre esquerda)
    //     if (canCastle(match, board, row, 0)) {
    //         moves.add(new Move(king.position, new Position(row, 2)));
    //     }
    // }

    // private boolean canCastle(ChessMatch match, Piece[][] board, int row, int rookCol) {
    //     Piece rook = board[row][rookCol];
    //     if (!(rook instanceof Rook) || ((Rook) rook).hasMoved()) return false;

    //     int step = rookCol == 0 ? -1 : 1;
    //     int end = rookCol == 0 ? 3 : 5;

    //     // Verifica casas vazias e não atacadas
    //     for (int col = king.position.y + step; col != end; col += step) {
    //         if (board[row][col] != null || isSquareUnderAttack(match, new Position(row, col))) {
    //             return false;
    //         }
    //     }
    //     return true;
    // }

    // En passant
    private void addEnPassantMoves(ChessMatch match, Pawn pawn, List<Move> moves) {
        Move lastMove = match.getLastMove();
        
        if (lastMove == null) return;

        Direction direction = pawn.getDirectionConsideringColor(Direction.NORTH);

        Position  attackedTile = pawn.position.neighbourTile(direction);

        if (isEnPassantOpportunity(pawn, lastMove, attackedTile)) {

            Move move = new Move(pawn.position, attackedTile);

            move.setEvent(Move.Event.EN_PASSANT);

            moves.add(move);
        }
    }

    private boolean isEnPassantOpportunity(Pawn pawn, Move lastMove, Position attackedTile) {
        // Implemente a lógica específica de en passant aqui
        return false; // Placeholder
    }

    // -- Validações de Xeque -- //
    public boolean isInCheck(ChessMatch match, PieceColor color) {
        Position kingPos = match.findKing(color).position;
        return isSquareUnderAttack(match, kingPos, color.opposite());
    }

    private boolean wouldCauseSelfCheck(ChessMatch match, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        simulator.simulateMove(match.getBoard(), piece, move); 

        boolean inCheck = isInCheck(match, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        simulator.revert(match.getBoard()); 

        return inCheck;
    }

    private boolean isSquareUnderAttack(ChessMatch match, Position square, PieceColor byColor) {
        return match.getAllPieces(byColor)
                    .stream()
                    .anyMatch(p -> p.allowedMoves(match.getBoard()).stream()
                    .anyMatch(m -> m.destination.equals(square)));
    }

    //Consegue simular uma jogada e a reverter (apenas uma jogada)
    private class MoveSimulator{

        static Move  simulatedMove;
        static Piece attackedPiece;
        static Piece movedPiece;

        public void simulateMove(Piece[][] board, Piece piece, Move move){

            simulatedMove = move;

            movedPiece = piece;

            attackedPiece = board[move.destination.x][move.destination.y];

            piece.apply(board, move);
        }

        public void revert(Piece[][] board){

            Move moveBack = new Move(simulatedMove.destination, simulatedMove.origin);

            movedPiece.apply(board, moveBack);
        }
    }
}
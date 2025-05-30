package com.example.chess.models;

import java.util.HashMap;
import java.util.List;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

public class ChessRules {

    //Usado no caso de Cheque a si mesmo (jogada deixa rei exposto)
    MoveSimulator              simulator   = new MoveSimulator();
    HashMap<Position, Boolean> attackCache = new HashMap<>();

    public List<Move> validateMoves(ChessMatch match, Piece piece, List<Move> moves) {

        attackCache.clear();

        //Adiciona jogadas especiais
        if (piece instanceof King) addCastlingMoves(match,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(match, (Pawn) piece, moves);

        //Filtra todas as jogadas que deixam o rei exposto
        moves.removeIf(move -> wouldCauseSelfCheck(match, piece, move));

        //Adiciona evento de cheque caso exista (verificar conflito onde várias jogadas especiais ocorrem)
        for(Move move : moves){
            if(causesCheck(match, piece, move)) move.setEvent(Event.CHECK);
        }

        return moves;
    }

    //Refazer castling, deepseek zaralhou legal o código
    // Roque (pequeno e grande)
    private void addCastlingMoves(ChessMatch match, King king, List<Move> moves) {
        if (king.hasMoved() || isInCheck(match, king.getColor())) return;

        Piece[][] board = match.getBoard();

        // Roque pequeno (torre direita)
        if (canCastle(match, board, 7)) {
            moves.add(new Move(king.position, new Position(0, 6)));
        }

        // Roque grande (torre esquerda)
        if (canCastle(match, board,  0)) {
            moves.add(new Move(king.position, new Position(0, 2)));
        }
    }

    private boolean canCastle(ChessMatch match, Piece[][] board, int rookCol) {
        Piece rook = board[0][rookCol];
        if (!(rook instanceof Rook) || ((Rook) rook).hasMoved()) return false;

        int step = rookCol == 0 ? -1 : 1;
        int end = rookCol == 0 ? 3 : 5;

        Piece king = match.findKing(match.getCurrentPlayer());

        // Verifica casas vazias e não atacadas
        for (int col = king.position.y + step; col != end; col += step) {
            
            if (board[0][col] != null) {
                return false;
            }

            if(isSquareUnderAttack(match, new Position(0, col), match.getOpponent())){
                return false;
            }
        }
        return true;
    }

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
    private boolean isInCheck(ChessMatch match, PieceColor color) {

        Position kingPos = match.findKing(color).position;

        return isSquareUnderAttack(match, kingPos, color.opposite());
    }
    
    private boolean isSquareUnderAttack(ChessMatch match, Position square, PieceColor byColor) {

        return attackCache.computeIfAbsent(square, pos -> {

                    return match.getAllPieces(byColor)
                                .stream()
                                .anyMatch(p -> p.allowedMoves(match.getBoard()).stream()
                                .anyMatch(m -> m.destination.equals(square)));
        });
    }

    private boolean causesCheck(ChessMatch match, Piece piece, Move move) {
        
        // 1. Simula o movimento
        simulator.simulateMove(match.getBoard(), piece, move);
        
        // 2. Verifica se o rei oposto está em xeque
        boolean causesCheck = isInCheck(match, piece.getColor().opposite());
        
        // 3. Reverte a simulação
        simulator.revert(match.getBoard());
        
        return causesCheck;
    }

    //Verifica se jogada não deixa o rei exposto a um ataque
    private boolean wouldCauseSelfCheck(ChessMatch match, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        simulator.simulateMove(match.getBoard(), piece, move); 

        boolean inCheck = isInCheck(match, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        simulator.revert(match.getBoard()); 

        return inCheck;
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

            int x = simulatedMove.destination.x;
            int y = simulatedMove.destination.y;

            Move moveBack = new Move(simulatedMove.destination, simulatedMove.origin);

            movedPiece.apply(board, moveBack);

            board[x][y] = attackedPiece;
        }
    }
}
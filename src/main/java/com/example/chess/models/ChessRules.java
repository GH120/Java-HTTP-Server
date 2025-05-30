package com.example.chess.models;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

public class ChessRules {

    //Usado no caso de Cheque a si mesmo (jogada deixa rei exposto)
    MoveSimulator              simulator   = new MoveSimulator();
    HashMap<Position, Boolean> attackCache = new HashMap<>();

    public List<Move> addSpecialMoves(ChessMatch match, Piece piece, List<Move> moves) {

        attackCache.clear();

        //Adiciona jogadas especiais
        if (piece instanceof King) addCastlingMoves(match,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(match, (Pawn) piece, moves);

        //Filtra todas as jogadas que deixam o rei exposto
        moves.removeIf(move -> wouldCauseSelfCheck(match, piece, move));

        //Adiciona evento de cheque caso exista (verificar conflito onde várias jogadas especiais ocorrem)
        for(Move move : moves){

            if(causesCheck(match, piece, move)){
                move.setEvent(Event.CHECK);
            }

            if(causesCheckMate(match, piece, move)){
                move.setEvent(Event.CHECKMATE);
            }
        }

        return moves;
    }

    // Roque (pequeno e grande)
    private void addCastlingMoves(ChessMatch match, King king, List<Move> moves) {

        if (king.hasMoved() || isInCheck(match, king.getColor())) return;

        Position KingSideRookPosition  = new Position(king.position.x, 7);
        Position QueenSideRookPosition = new Position(king.position.x, 0);

        // Roque pequeno (torre direita)
        if (canCastle(match, match.getBoard(), KingSideRookPosition)) {

            Position kingDestination = new Position(king.position.x, king.position.y + 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING));
        }

        // Roque grande (torre esquerda)
        if (canCastle(match, match.getBoard(),  QueenSideRookPosition)) {

            Position kingDestination = new Position(king.position.x, king.position.y - 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING));
        }
    }

    private boolean canCastle(ChessMatch match, Piece[][] board, Position rookPosition) {

        Piece rook = board[rookPosition.x][rookPosition.y];

        if (!(rook instanceof Rook) || ((Rook) rook).hasMoved()) return false;

        boolean queenSide = rookPosition.y == 0;

        int step = queenSide ? -1 : 1;
        int end  = queenSide ?  1 : 6;

        Piece king = match.findKing(match.getCurrentPlayer());

        // Verifica casas vazias e não atacadas
        for (int col = king.position.y + step; col != end; col += step) {
            
            if (board[king.position.x][col] != null) {
                return false;
            }

            if(isSquareUnderAttack(match, new Position(king.position.x, col), match.getOpponent())){
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
                                .anyMatch(
                                    p -> p.allowedMoves(match.getBoard())
                                          .stream()
                                          .anyMatch(m -> m.destination.equals(square))
                                );
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

    private boolean causesCheckMate(ChessMatch match, Piece piece, Move move){

        Boolean causesCheckMate;

        PieceColor enemyColor = piece.getColor().opposite();

        simulator.simulateMove(match.getBoard(), piece, move);

        causesCheckMate = isInCheckMate(match, enemyColor);

        simulator.revert(match.getBoard());

        return causesCheckMate;
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

    //Ineficiente, poderia verificar apenas as peças atacando o quadrado do rei
    public boolean isInCheckMate(ChessMatch match, PieceColor color){

        //1. Primeiro verifica se o rei está em xeque
        if(!isInCheck(match, color)){
            return false;
        }

        //2. Verifica se existe algum movimento que tira do xeque
        for(Piece piece : match.getAllPieces(color)){
            for(Move move : piece.allowedMoves(match.getBoard())){
                if(!wouldCauseSelfCheck(match, piece, move)){
                    return false;
                }
            }
        }


        //Se não existir nenhum movimento que salva o rei, é xeque-mate
        return true;
    }

    //Consegue simular jogadas e a reverter
    //TODO: Ver possível bug que jogadas simuladas de rei e torre podem setar elas como hasMoved
    private class MoveSimulator{

        private Stack<Move>  simulatedMoves = new Stack<>();
        private Stack<Piece> attackedPieces = new Stack<>();
        private Stack<Piece> movedPieces    = new Stack<>();

        public void simulateMove(Piece[][] board, Piece piece, Move move){

            simulatedMoves.push(move);

            movedPieces.push(piece);

            attackedPieces.push(board[move.destination.x][move.destination.y]);

            piece.apply(board, move);
        }

        public void revert(Piece[][] board){

            Piece attackedPiece = attackedPieces.pop();
            Piece movedPiece    = movedPieces.pop();
            Move  simulatedMove = simulatedMoves.pop();

            int x = simulatedMove.destination.x;
            int y = simulatedMove.destination.y;

            Move moveBack = new Move(simulatedMove.destination, simulatedMove.origin);

            movedPiece.apply(board, moveBack);

            board[x][y] = attackedPiece;
        }
    }
}
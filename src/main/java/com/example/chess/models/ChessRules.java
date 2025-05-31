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

    public List<Move> validateMoves(ChessMatch match, Piece piece, List<Move> moves) {

        attackCache.clear();

        //Adiciona jogadas especiais
        if (piece instanceof King) addCastlingMoves(match,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(match, (Pawn) piece, moves);

        //Filtra todas as jogadas que deixam o rei exposto
        moves.removeIf(move -> wouldCauseSelfCheck(match, piece, move));

        return moves;
    }

    public List<Move> evaluateCheckEvents(ChessMatch match, Piece piece, List<Move> moves){
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

        if (match.hasMoved(king) || isInCheck(match, king.getColor())) return;

        Position KingsideRookPosition  = new Position(king.position.x, 7);
        Position QueensideRookPosition = new Position(king.position.x, 0);

        // Roque pequeno (torre direita)
        if (canCastle(match, match.getBoard(), KingsideRookPosition)) {

            Position kingDestination = new Position(king.position.x, king.position.y + 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING));
        }

        // Roque grande (torre esquerda)
        if (canCastle(match, match.getBoard(),  QueensideRookPosition)) {

            Position kingDestination = new Position(king.position.x, king.position.y - 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING));
        }
    }

    private boolean canCastle(ChessMatch match, Piece[][] board, Position rookPosition) {

        Piece rook = board[rookPosition.x][rookPosition.y];

        if (!(rook instanceof Rook) || (match.hasMoved(rook))) return false;

        boolean queenSide = rookPosition.y == 0;

        int step = queenSide ? -1 : 1;
        int end  = queenSide ?  1 : 6;

        Piece king = match.findKing(match.getCurrentColor());

        // Verifica casas vazias e não atacadas
        for (int col = king.position.y + step; col != end; col += step) {
            
            if (board[king.position.x][col] != null) {
                return false;
            }

            if(isSquareUnderAttack(match, new Position(king.position.x, col), match.getOpponentColor())){
                return false;
            }
        }
        return true;
    }

    // En passant
    private void addEnPassantMoves(ChessMatch match, Pawn pawn, List<Move> moves) {
        Move lastMove = match.getLastMove();
        
        if (lastMove == null) return;

        if (isEnPassantOpportunity(pawn, lastMove)) {

            //Consegue posição do peão assassino no enpassant
            Direction forward = pawn.getDirection(Direction.NORTH);

            Position  targetTile = pawn.position.neighbourTile(forward);

            Move move = new Move(pawn.position, targetTile);

            //Adiciona a vitima do enpassant como target dele, e adiciona jogada às jogadas válidas
            Position attackedPawnPosition = lastMove.destination;

            Piece attackedPawn = match.getPiece(attackedPawnPosition);

            move.setEvent(Move.Event.EN_PASSANT.setTarget(attackedPawn)); 

            moves.add(move);

        }
    }

    private boolean isEnPassantOpportunity(Pawn pawn, Move lastMove) {
        // Implemente a lógica específica de en passant aqui

        if(lastMove.event == Event.TWOTILESKIP){
            
            Position  attackedPawnPosition = lastMove.destination;
            
            boolean areSideNeighbours = Math.abs(attackedPawnPosition.x - pawn.position.x) == 1;

            boolean sameHeight = attackedPawnPosition.y == pawn.position.y;

            return areSideNeighbours && sameHeight;
        }

        return false;
    }
    
    ///////////////////////////////
    // -- Validações de Xeque -- //
    ///////////////////////////////
    
    private boolean isInCheck(ChessMatch match, PieceColor color) {

        Position kingPos = match.findKing(color).position;

        return isSquareUnderAttack(match, kingPos, color.opposite());
    }
    
    //Não leva em conta o en-passant
    private boolean isSquareUnderAttack(ChessMatch match, Position square, PieceColor byColor) {

        //Nenhum movimento especial é um ataque, salvo o en-passant
        //Para o caso de movimento do pawn, levar em conta se o Event do move é um ataque ou movimento
        
        return attackCache.computeIfAbsent(square, pos -> {

                    return match.getAllPieces(byColor)
                                .stream()
                                .anyMatch(
                                    p -> p.allowedMoves(match.getBoard())
                                         .stream()
                                         .filter(  m -> m.event == Event.ATTACK) //Ignora os movimentos de peão
                                         .anyMatch(m -> m.destination.equals(square))
                                );
        });
    }

    private boolean causesCheck(ChessMatch match, Piece piece, Move move) {
        
        // 1. Simula o movimento
        simulator.simulateMove(match, piece, move);
        
        // 2. Verifica se o rei oposto está em xeque
        boolean causesCheck = isInCheck(match, piece.getColor().opposite());
        
        // 3. Reverte a simulação
        simulator.revert(match);
        
        return causesCheck;
    }

    private boolean causesCheckMate(ChessMatch match, Piece piece, Move move){

        Boolean causesCheckMate;

        PieceColor enemyColor = piece.getColor().opposite();

        simulator.simulateMove(match, piece, move);

        causesCheckMate = isInCheckMate(match, enemyColor);

        simulator.revert(match);

        return causesCheckMate;
    }

    //Verifica se jogada não deixa o rei exposto a um ataque
    private boolean wouldCauseSelfCheck(ChessMatch match, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        simulator.simulateMove(match, piece, move); 

        boolean inCheck = isInCheck(match, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        simulator.revert(match); 

        return inCheck;
    }

    //Ineficiente, poderia verificar apenas as peças atacando o quadrado do rei
    public boolean isInCheckMate(ChessMatch match, PieceColor color){

        //1. Primeiro verifica se o rei está em xeque
        if(!isInCheck(match, color)){
            return false;
        }

        King king = match.findKing(color);
        
        //2. verifica se o rei pode escapar
        for (Move move : king.allowedMoves(match.getBoard())) {
            if (!wouldCauseSelfCheck(match, king, move)) {
                return false;
            }
        }

        //3. Verifica se existe algum movimento de ataque que tira do xeque
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
    private class MoveSimulator{

        private Stack<Move>  simulatedMoves = new Stack<>();
        private Stack<Piece> attackedPieces = new Stack<>();
        private Stack<Piece> movedPieces    = new Stack<>();

        public void simulateMove(ChessMatch match, Piece piece, Move move){

            simulatedMoves.push(move);

            movedPieces.push(piece);

            attackedPieces.push(match.getPiece(move.destination));

            match.play(piece, move);
        }

        public void revert(ChessMatch match){

            Piece attackedPiece = attackedPieces.pop();
            Piece movedPiece    = movedPieces.pop();
            Move  simulatedMove = simulatedMoves.pop();

            Move moveBack = new Move(simulatedMove.destination, simulatedMove.origin);

            match.placePiece(attackedPiece, simulatedMove.destination);

            match.play(movedPiece, moveBack);

            match.forgetMove()
                 .forgetMove(); //Esquece os dois movimentos simulados
        }

        //Tratar casos de En-passant, Castle usando o event do simulatedMoves (não vale a pena separar ataque de movimento)
        private void treatSideEffects(Piece[][] board, Piece piece, Move move){
            
            
        }
    }
}
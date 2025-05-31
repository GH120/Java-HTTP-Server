package com.example.chess.models;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

public class ChessRules {

    HashMap<Position, Boolean> attackCache = new HashMap<>();


    ///////////////////////////
    // -- Métodos Públicos --//
    ///////////////////////////

    /** Adiciona jogadas especiais e filtra as que causam autoxeque*/
    public List<Move> validateMoves(ChessModel match, Piece piece, List<Move> moves) {

        attackCache.clear();

        //Adiciona jogadas especiais
        if (piece instanceof King) addCastlingMoves(match,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(match, (Pawn) piece, moves);
        if (piece instanceof Pawn) addPromotionMove(match,  (Pawn) piece, moves);

        //Filtra todas as jogadas que deixam o rei exposto
        moves.removeIf(move -> wouldCauseSelfCheck(match, piece, move));

        return moves;
    }

    ////////////////////////////
    // -- Jogadas Especiais --//
    ////////////////////////////
    
    // Roque (pequeno e grande)
    private void addCastlingMoves(ChessModel match, King king, List<Move> moves) {

        if (match.hasMoved(king) || isInCheck(match, king.getColor())) return;

        Position KingsideRookPosition  = new Position(king.position.x, 7); //Adicionar enum chesspositions?
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

    private boolean canCastle(ChessModel match, Piece[][] board, Position rookPosition) {

        Piece rook = board[rookPosition.x][rookPosition.y];

        if (!(rook instanceof Rook) || (match.hasMoved(rook))) return false;

        boolean queenSide = rookPosition.y == 0;

        int step = queenSide ? -1 : 1;
        int end  = queenSide ?  1 : 6;

        Piece king = match.findKing(rook.getColor());

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
    private void addEnPassantMoves(ChessModel match, Pawn pawn, List<Move> moves) {
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
    

    // Promoção
    private void addPromotionMove(ChessModel match, Pawn pawn, List<Move> moves){

        int lastRow = pawn.getColor() == PieceColor.WHITE ? 7 : 0; //Adicionar enum chessPositions?

        for(Move move : moves){

            if(move.destination.x == lastRow){
                move.setEvent(Event.PROMOTION);
            }
        }
    }

    ///////////////////////////////
    // -- Validações de Xeque -- //
    ///////////////////////////////
    
    public boolean isInCheck(ChessModel match, PieceColor color) {

        Position kingPos = match.findKing(color).position;

        return isSquareUnderAttack(match, kingPos, color.opposite());
    }
    
    //Não leva em conta o en-passant
    private boolean isSquareUnderAttack(ChessModel match, Position square, PieceColor byColor) {

        //Nenhum movimento especial é um ataque, salvo o en-passant
        //Para o caso de movimento do pawn, levar em conta se o Event do move é um ataque ou movimento
        
        return attackCache.computeIfAbsent(square, pos -> {

                    return match.getAllPieces(byColor)
                                .stream()
                                .anyMatch(
                                    p -> p.defaultMoves(match.getBoard())
                                         .stream()
                                         .filter(  m -> m.event == Event.ATTACK) //Ignora os movimentos de peão
                                         .anyMatch(m -> m.destination.equals(square))
                                );
        });
    }

    //Verifica se jogada não deixa o rei exposto a um ataque
    private boolean wouldCauseSelfCheck(ChessModel match, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        match.play(piece, move); 

        boolean inCheck = isInCheck(match, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        match.revertLastMove(); 

        return inCheck;
    }

    public boolean isDraw(ChessModel match, PieceColor color){

        King king = match.findKing(color);
        
        //1. verifica se o rei pode escapar
        for (Move move : king.defaultMoves(match.getBoard())) {
            if (!wouldCauseSelfCheck(match, king, move)) {
                return false;
            }
        }

        //2. Verifica se existe algum movimento de ataque que tira do xeque
        for(Piece piece : match.getAllPieces(color)){

            List<Move> validMoves = validateMoves(match, piece, piece.defaultMoves(match.getBoard()));

            if(validMoves.size() > 0) return false;
        }

        return true;
    }

    //Ineficiente, poderia verificar apenas as peças atacando o quadrado do rei
    public boolean isInCheckMate(ChessModel match, PieceColor color){

        return isInCheck(match, color) && isDraw(match, color);
    }
}
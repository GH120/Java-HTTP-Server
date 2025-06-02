package com.example.chess.models;

import java.util.HashMap;
import java.util.List;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

public class ChessRules {

    HashMap<Position, Boolean> attackCache = new HashMap<>();


    ///////////////////////////
    // -- Métodos Públicos --//
    ///////////////////////////

    /** Adiciona jogadas especiais e filtra as que causam autoxeque
     *  Efeitos colaterais das jogadas especiais ->NÃO<- são calculados, 
     *  e são executados apenas quando a jogada é efetuada no chessModel 
     *  ex: matar peão no enpassant e mover torre no castle
     *  retorna lista enriquecida e filtrada de todas as jogadas possíveis 
    */
    public List<Move> validateMoves(ChessModel model, Piece piece, List<Move> moves) {

        attackCache.clear();

        //Adiciona jogadas especiais
        if (piece instanceof King) addCastlingMoves(model,  (King) piece, moves);
        if (piece instanceof Pawn) addEnPassantMoves(model, (Pawn) piece, moves);
        if (piece instanceof Pawn) addPromotionMove(model,  (Pawn) piece, moves);

        //Filtra todas as jogadas que deixam o rei exposto
        moves.removeIf(move -> wouldCauseSelfCheck(model, piece, move));

        return moves;
    }

    ////////////////////////////
    // -- Jogadas Especiais --//
    ////////////////////////////
    
    // Roque (pequeno e grande)
    private void addCastlingMoves(ChessModel model, King king, List<Move> moves) {

        if (model.hasMoved(king) || isInCheck(model, king.getColor())) return;

        Piece kingsideRook  = model.getPiece(new Position(king.position.x, 7)); //Adicionar enum chesspositions?
        Piece queensideRook = model.getPiece(new Position(king.position.x, 0));

        // Roque pequeno (torre direita)
        if (canCastle(model, king, kingsideRook)) {

            Position kingDestination = new Position(king.position.x, king.position.y + 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING.setTarget(kingsideRook)));
        }

        // Roque grande (torre esquerda)
        if (canCastle(model, king, queensideRook)) {

            Position kingDestination = new Position(king.position.x, king.position.y - 2);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING.setTarget(queensideRook)));
        }
    }

    private boolean canCastle(ChessModel model, Piece king, Piece rook) {

        if (!(rook instanceof Rook) || (model.hasMoved(rook))) return false;

        boolean queenSide = rook.position.y == 0;

        int step = queenSide ? -1 : 1;
        int end  = queenSide ?  1 : 6;

        // Verifica casas vazias e não atacadas
        for (int col = king.position.y + step; col != end; col += step) {

            Position square = new Position(king.position.x, col);
            
            if (model.getBoard()[king.position.x][col] != null) {
                return false;
            }

            if(isSquareUnderAttack(model, square, model.getOpponentColor())){ //Verificar se esse getOpponentColor não gera bugs 
                return false;
            }
        }
        return true;
    }

    // En passant
    private void addEnPassantMoves(ChessModel model, Pawn pawn, List<Move> moves) {
        Move lastMove = model.getLastMove();
        
        if (lastMove == null) return;

        if (isEnPassantOpportunity(pawn, lastMove)) {

            //Consegue posição do peão assassino no enpassant
            Direction forward = pawn.getDirection(Direction.NORTH);

            Position  targetTile = pawn.position.moveTo(forward);

            Move move = new Move(pawn.position, targetTile);

            //Adiciona a vitima do enpassant como target dele, e adiciona jogada às jogadas válidas
            Position attackedPawnPosition = lastMove.destination;

            Piece attackedPawn = model.getPiece(attackedPawnPosition);

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
    private void addPromotionMove(ChessModel model, Pawn pawn, List<Move> moves){

        int lastRow = pawn.getColor() == PlayerColor.WHITE ? 7 : 0; //Adicionar enum chessPositions?

        for(Move move : moves){

            if(move.destination.x == lastRow){
                move.setEvent(Event.PROMOTION);
            }
        }
    }

    ///////////////////////////////
    // -- Validações de Xeque -- //
    ///////////////////////////////
    
    public boolean isInCheck(ChessModel model, PlayerColor color) {

        Position kingPos = model.findKing(color).position;

        return isSquareUnderAttack(model, kingPos, color.opposite());
    }
    
    //Não leva em conta o en-passant
    private boolean isSquareUnderAttack(ChessModel model, Position square, PlayerColor byColor) {

        //Nenhum movimento especial é um ataque, salvo o en-passant
        //Para o caso de movimento do pawn, levar em conta se o Event do move é um ataque ou movimento
        
        return attackCache.computeIfAbsent(square, pos -> {

                    return model.getAllPieces(byColor)
                                .stream()
                                .anyMatch(
                                    p -> p.defaultMoves(model.getBoard())
                                         .stream()
                                         .filter(  m -> m.event == Event.ATTACK) //Ignora os movimentos de peão
                                         .anyMatch(m -> m.destination.equals(square))
                                );
        });
    }

    //Verifica se jogada não deixa o rei exposto a um ataque
    private boolean wouldCauseSelfCheck(ChessModel model, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        model.play(piece, move); 

        boolean inCheck = isInCheck(model, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        model.revertLastMove(); 

        return inCheck;
    }

    public boolean isDraw(ChessModel model, PlayerColor color){

        King king = model.findKing(color);
        
        //1. verifica se o rei pode escapar
        for (Move move : king.defaultMoves(model.getBoard())) {
            if (!wouldCauseSelfCheck(model, king, move)) {
                return false;
            }
        }

        //2. Verifica se existe algum movimento de ataque que tira do xeque
        for(Piece piece : model.getAllPieces(color)){

            List<Move> validMoves = validateMoves(model, piece, piece.defaultMoves(model.getBoard()));

            if(validMoves.size() > 0) return false;
        }

        return true;
    }

    //Ineficiente, poderia verificar apenas as peças atacando o quadrado do rei
    public boolean isInCheckMate(ChessModel model, PlayerColor color){

        return isInCheck(model, color) && isDraw(model, color);
    }
}
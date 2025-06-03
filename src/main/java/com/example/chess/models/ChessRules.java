package com.example.chess.models;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.Bishop;
import com.example.chess.models.chesspieces.King;
import com.example.chess.models.chesspieces.Knight;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.chesspieces.Rook;

/** Validador de regras do Xadrez;
 * Responsabilidades: 
 * 1. Validar jogadas básicas; 
 * 2. Adicionar jogadas especiais e eventos
 * 3. Verifica eventos de mudança do estado do jogo (Cheque, Empate, Promoção...)
 */
public class ChessRules {

    HashMap<Position, Boolean> attackCache = new HashMap<>();


    //////////////////////////
    // -- Método Público -- //
    //////////////////////////

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

        Piece kingsideRook  = model.getPiece(new Position(7, king.position.y)); //Adicionar enum chesspositions?
        Piece queensideRook = model.getPiece(new Position(0, king.position.y));

        // Roque pequeno (torre direita)
        if (canCastle(model, king, kingsideRook)) {

            Position kingDestination = new Position(king.position.x + 2, king.position.y);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING.setTarget(kingsideRook)));
        }

        // Roque grande (torre esquerda)
        if (canCastle(model, king, queensideRook)) {

            Position kingDestination = new Position(king.position.x - 2, king.position.y);

            Move move = new Move(king.position, kingDestination);

            moves.add(move.setEvent(Event.CASTLING.setTarget(queensideRook)));
        }
    }

    private boolean canCastle(ChessModel model, Piece king, Piece rook) {

        if (!(rook instanceof Rook) || (model.hasMoved(rook))) return false;

        boolean queenSide = rook.position.x == 0;

        int step = queenSide ? -1 : 1;
        int end  = queenSide ?  1 : 6;

        // Verifica casas vazias e não atacadas
        for (int col = king.position.x + step; col != end; col += step) {

            Position square = new Position(col, king.position.y);
            
            if (model.getBoard()[col][king.position.y] != null) {
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

            //Adiciona a vitima do enpassant como target dele
            Position attackedPawnPosition = lastMove.destination;

            Piece attackedPawn = model.getPiece(attackedPawnPosition);


            //Consegue direção do peão assassino no enpassant
            Direction forward = pawn.getDirection(Direction.NORTH);

            Position  targetTile = new Position(attackedPawnPosition.x, pawn.position.y + forward.y);

            Move move = new Move(pawn.position, targetTile);

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

            if(move.destination.y == lastRow){
                move.setEvent(Event.PROMOTION);
            }
        }
    }

    ///////////////////////////////////////////////////////////
    // -- Validações de Xeque e Empate: Interface Pública -- //
    ///////////////////////////////////////////////////////////
    
    public boolean isInCheck(ChessModel model, PlayerColor color) {

        Position kingPos = model.findKing(color).position;

        return isSquareUnderAttack(model, kingPos, color.opposite());
    }

    //Ineficiente, poderia verificar apenas as peças atacando o quadrado do rei
    public boolean isInCheckMate(ChessModel model, PlayerColor color){

        return isInCheck(model, color) && isStalemate(model, color);
    }

    public boolean isDraw(ChessModel model, PlayerColor color) {

        if (isStalemate(model, color))     return true;
        if (isInsufficientMaterial(model)) return true;
        // 3. Repetição de posição (não implementado aqui)
        // 4. Regra dos 50 movimentos (não implementado aqui)
        return false;
    }

    ///////////////////////////////////////////////////////////
    // -- Validações de Xeque e Empate: Métodos Privados -- //
    ///////////////////////////////////////////////////////////
    
    /**
     * Checks if a square is under attack.  
     * WARNING: Ignores en passant (must be handled separately).  
     * Extremamente ineficiente (O(n²)), refatorar depois
     */
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

    /** Verifica se jogada não deixa o rei exposto a um ataque */
    private boolean wouldCauseSelfCheck(ChessModel model, Piece piece, Move move) {

        //Simula jogada, guardando estado inicial
        model.play(piece, move); 

        boolean inCheck = isInCheck(model, piece.getColor());

        //Retorna tabuleiro e peça ao estado inicial
        model.revertLastMove(); 

        //Cache de ataque para a jogada simulada é eliminado (Verificar ineficiência)
        attackCache.clear();

        return inCheck;
    }

    private boolean isStalemate(ChessModel model, PlayerColor color){

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


    /////////////////////////////////////////////////
    // -- Condições de Empate: Métodos Privados -- //
    /////////////////////////////////////////////////
    
    private boolean isInsufficientMaterial(ChessModel model) {
        Set<Piece> whitePieces = model.getAllPieces(PlayerColor.WHITE);
        Set<Piece> blackPieces = model.getAllPieces(PlayerColor.BLACK);

        // Caso 1: Apenas os dois reis
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }

        // Caso 2: Rei + Bispo vs Rei
        if (whitePieces.size() == 2 && blackPieces.size() == 1) {
            if (hasOnlyKingAndBishop(whitePieces)) {
                return true;
            }
        }
        if (blackPieces.size() == 2 && whitePieces.size() == 1) {
            if (hasOnlyKingAndBishop(blackPieces)) {
                return true;
            }
        }

        // Caso 3: Rei + Cavalo vs Rei
        if (whitePieces.size() == 2 && blackPieces.size() == 1) {
            if (hasOnlyKingAndKnight(whitePieces)) {
                return true;
            }
        }
        if (blackPieces.size() == 2 && whitePieces.size() == 1) {
            if (hasOnlyKingAndKnight(blackPieces)) {
                return true;
            }
        }

        // Caso 4: Rei + Bispo vs Rei + Bispo (mesma cor)
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            if (hasOnlyKingAndBishop(whitePieces) && hasOnlyKingAndBishop(blackPieces)) {
                // Verifica se os bispos são da mesma cor
                Piece whiteBishop = whitePieces.stream().filter(p -> p instanceof Bishop).findFirst().orElse(null);
                Piece blackBishop = blackPieces.stream().filter(p -> p instanceof Bishop).findFirst().orElse(null);
                if (whiteBishop != null && blackBishop != null) {
                    boolean whiteSquareBishop = (whiteBishop.position.x + whiteBishop.position.y) % 2 == 0;
                    boolean blackSquareBishop = (blackBishop.position.x + blackBishop.position.y) % 2 == 0;
                    return whiteSquareBishop == blackSquareBishop;
                }
            }
        }

        return false;
    }

    private boolean hasOnlyKingAndBishop(Set<Piece> pieces) {
        return pieces.size() == 2 &&
            pieces.stream().anyMatch(p -> p instanceof King) &&
            pieces.stream().anyMatch(p -> p instanceof Bishop);
    }

    private boolean hasOnlyKingAndKnight(Set<Piece> pieces) {
        return pieces.size() == 2 &&
            pieces.stream().anyMatch(p -> p instanceof King) &&
            pieces.stream().anyMatch(p -> p instanceof Knight);
    }
}
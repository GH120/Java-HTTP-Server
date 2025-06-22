package com.example.chess.controlers;

import java.util.HashMap;
import java.util.List;

import com.example.chess.models.ChessModel;
import com.example.chess.models.ChessRules;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.gamestart.DefaultStartingPieces;


//Transformar ele numa thread?
//Controlaria as ações do usuário, como escolher jogadas ou sair da partida
//Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado

/**Controlador da partida de xadrez;
 * Responsabilidades: 
 * 1. Tratar erros de jogadas do usuário; 
 * 2. armazenar estado do jogo;
 * 3. notificar interessados sobre eventos
 */
public class ChessMatch {

    private GameState  state;
    private Player     white;
    private Player     black;

    public  final MatchSynchronizer semaphor; //Responsabilidade de sincronização fica fora da partida, expõe métodos
    private final MatchNotifier     notifier;
    private final ChessRules        chessRules;
    private final ChessModel        chessModel;

    private final HashMap<Position, List<Move>> moveCache;
    
    public enum GameState {NORMAL, CHECK, CHECKMATE, DRAW, PROMOTION, STARTED, EXITED}

    public ChessMatch(Player player, Player opponent) {
        moveCache  = new HashMap<>();
        chessRules = new ChessRules();
        chessModel = new ChessModel(new DefaultStartingPieces());
        notifier   = new MatchNotifier();
        semaphor   = new MatchSynchronizer();
        white = player;
        black = opponent;
        state = GameState.STARTED;
    }

    /**Controle para efetuar jogada de Xadrez, solta erros se jogada for inconsistente */
    public void playMove(Player player, Move move) throws ChessError{
        
        Piece      piece = chessModel.getPiece(move.origin);
        //Verifica inconsistências na requisição da jogada
        if(state == GameState.CHECKMATE) throw new GameHasAlreadyEnded();
        if(state == GameState.DRAW)      throw new GameHasAlreadyEnded();
        if(state == GameState.EXITED)    throw new GameHasAlreadyEnded();
        if(state == GameState.PROMOTION) throw new PendingPromotion();
        if(piece == null)                throw new InvalidMove();
        
        List<Move> moves = getAllPossibleMoves(move.origin);

        if(!moves.contains(move))                       throw new InvalidMove();
        if(piece.color != chessModel.getCurrentColor()) throw new NotPlayerTurn();

        //Uma vez validada, registra jogada no modelo, atualiza estado do jogo e notifica aos observadores
        chessModel.play(piece, move);

        notifier.notifyMove(move, chessModel.getCurrentColor());
        
        updateGameState(move);

        moveCache.clear();
    }

    /** Notifica todas as jogadas possíveis para os observers */
    public void showPossibleMoves(Position position){

        notifier.notifyPossibleMoves(getAllPossibleMoves(position));
    }


    public void choosePromotion(Pawn.Promotion promotion) throws NoPromotionEvent{

        if(state != GameState.PROMOTION) 
            throw new NoPromotionEvent();

        chessModel.choosePromotion(promotion);

        updateGameState(null);
    }

    public void quit(){
        state = GameState.EXITED;

        notifier.notifyStateChange(state);
    }


    private void updateGameState(Move move){

        if(chessRules.isInCheckMate(chessModel, chessModel.getCurrentColor())){
            state = GameState.CHECKMATE;
        }
        else if(chessRules.isDraw(chessModel, chessModel.getCurrentColor())){
            state = GameState.DRAW;
        }
        else if(chessRules.isInCheck(chessModel, chessModel.getCurrentColor())){
            state = GameState.CHECK;
        }
        else{
            state = GameState.NORMAL;
        }

        notifier.notifyStateChange(state);

        //Depois de avisar se houve cheque ou o jogo acabou, verifica promoção
        if(move != null && move.event == Event.PROMOTION){
            notifier.notifyPromotionRequired(move.destination);

            state = GameState.PROMOTION;
        }
    }

    private List<Move> getAllPossibleMoves(Position position){

        return moveCache.computeIfAbsent(position, pos ->{
        
            Piece piece = chessModel.getPiece(position);

            List<Move> defaultMoves = piece.defaultMoves(chessModel.getBoard());

            List<Move> allowedMoves = this.chessRules.validateMoves(chessModel, piece, defaultMoves);

            return allowedMoves;
        });
    }

    public Player getBlack() {
        return black;
    }

    public Player getWhite() {
        return white;
    }

    public Player getOpponent(Player player){
        return player.name == white.name ? white : black;
    }

    public ChessModel getChessModel() {
        return chessModel;
    }

    public GameState getState() {
        return state;
    }

    public void addObserver(MatchObserver observer){
        notifier.addObserver(observer);
    }

    //Classes internas

    //Usar um countdown latch? muito mais claro e coeso
    public class MatchSynchronizer{

        private boolean moveReceived = false;

        public synchronized void waitForMove() throws InterruptedException {
            while (!moveReceived) {
                wait(); // Libera o lock até receber notificação
            }
            moveReceived = false; // Reseta para próxima jogada
        }


        //Ambos wait e notifyAll tem que estarem em um bloco synchronized
        public synchronized void notifyMove() {
            moveReceived = true;
            notifyAll(); // Libera as threads bloqueadas
        }
    }



    //Erros de Jogada
    public class ChessError extends Exception{

        ChessError(){
            super("Jogada inválida");

            notifier.notifyError(getLocalizedMessage());
        }
    }

    public class InvalidMove extends ChessError{

    }

    public class NotPlayerTurn extends ChessError{

    }

    public class PendingPromotion extends ChessError{

    }

    public class NoPromotionEvent extends ChessError{

    }

    public class GameHasAlreadyEnded extends ChessError{

    }

}
package com.example.chess.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import com.example.chess.models.ChessModel;
import com.example.chess.models.ChessRules;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Player;
import com.example.chess.models.PlayerColor;
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
 * 3. notificar interessados sobre eventos;
 */
public class ChessMatch {

    //Campos de estado
    private GameState             state;
    private Map<Player, Integer>  playerTimeRamaining;

    //Campos imutáveis
    private final Player white;
    private final Player black;

    //Componentes
    public  final MatchSynchronizer semaphor; //Responsabilidade de sincronização fica fora da partida, expõe métodos (acho melhor tornar isso um observer e colocar um método wait, mas corre o risco de ficar muito complexo)
    private final MatchNotifier     notifier;
    private final ChessRules        chessRules;
    private final ChessModel        chessModel;

    //Cache
    private final Map<Position, List<Move>> moveCache;
    
    public enum GameState {NORMAL, CHECK, CHECKMATE, DRAW, PROMOTION, STARTED, EXITED, TIMEOUT}

    public ChessMatch(Player player, Player opponent) {

        white = player;
        black = opponent;
        state = GameState.STARTED;
        
        playerTimeRamaining = new HashMap<>();
        playerTimeRamaining.put(white, 10);
        playerTimeRamaining.put(black, 10);

        moveCache  = new HashMap<>();
        chessRules = new ChessRules();
        chessModel = new ChessModel(new DefaultStartingPieces());
        notifier   = new MatchNotifier();
        semaphor   = new MatchSynchronizer();
    }

    /**Controle para efetuar jogada de Xadrez, solta erros se jogada for inconsistente */
    public void playMove(Player player, Move move) throws ChessError{
        
        Piece      piece = chessModel.getPiece(move.origin);

        //Verifica inconsistências na requisição da jogada
        if(state == GameState.CHECKMATE) throw new GameHasAlreadyEnded();
        if(state == GameState.DRAW)      throw new GameHasAlreadyEnded();
        if(state == GameState.EXITED)    throw new GameHasAlreadyEnded();
        if(state == GameState.TIMEOUT)   throw new GameHasAlreadyEnded();
        if(state == GameState.PROMOTION) throw new PendingPromotion();
        if(piece == null)                throw new InvalidMove();
        
        List<Move> moves = getAllPossibleMoves(move.origin);

        if(!moves.contains(move))                       throw new InvalidMove();
        // if(piece.color != getColor(player))             throw new NotPlayerPiece(); //corrigir bug
        if(piece.color != chessModel.getCurrentColor()) throw new NotPlayerTurn();

        //Uma vez validada, registra jogada no modelo, atualiza estado do jogo e notifica aos observadores
        chessModel.play(piece, move);

        notifier.notifyMove(move, chessModel.getCurrentColor());
        
        updateGameState(move);

        moveCache.clear();

        System.out.println(getTime(player));
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

    public void checkTimeOut(){
        
        Optional<Entry<Player, Integer>> playerTimeout = playerTimeRamaining.entrySet()
                                                                  .stream()
                                                                  .filter(entry -> entry.getValue() == 0)
                                                                  .findFirst();

        if(playerTimeout.isPresent()){
            
            state = GameState.TIMEOUT;

            notifier.notifyStateChange(state);

            System.out.println("TEMPO TERMINOU");
        }
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

    //Getters de utilidade
    public Player getBlack() {
        return black;
    }

    public Player getWhite() {
        return white;
    }

    public Player getOpponent(Player player){
        return player.name == white.name ? white : black;
    }

    public PlayerColor getColor(Player player){
        return player.name == white.name ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    public Player getPlayer(PlayerColor color){
        return color == PlayerColor.WHITE? white : black;
    }

    public Integer getTime(Player player){
        return playerTimeRamaining.get(player);
    }

    public void updateTime(Player player, int time){
        playerTimeRamaining.put(player, time);
    }

    public ChessModel getChessModel() {
        return chessModel;
    }

    public GameState getState() {
        return state;
    }

    public Player getCurrentPlayer(){
        return chessModel.getCurrentColor() == PlayerColor.WHITE? white : black;
    }

    public void addObserver(MatchObserver observer){
        notifier.addObserver(observer);
    }

    //Classes internas

    //Usar um countdown latch?
    //Guardar estado do último jogador para tratar jogadas repetidas?
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

    public class NotPlayerPiece extends ChessError{

    }

}
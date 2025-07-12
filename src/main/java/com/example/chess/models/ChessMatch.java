package com.example.chess.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import com.example.chess.models.Move.Event;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.models.gamestart.DefaultStartingPieces;
import com.example.chess.services.MatchNotifier;
import com.example.chess.services.MatchObserver;


//Controlaria as ações do usuário, como escolher jogadas ou sair da partida
//Teria um validador de jogadas baseado no estado de jogo, estado de jogo armazenado
//Observer é mais "event-driven" (ideal para notificações).
//Decorator é mais "behavioral" (ideal para modificar comportamentos). (com side-effects)

/**Controlador da partida de xadrez;
 * Responsabilidades: 
 * 1. Tratar erros de jogadas do usuário; 
 * 2. armazenar estado do jogo;
 * 3. notificar interessados sobre eventos;
 */
public class ChessMatch {

    //Campos de estado
    private GameState             state;
    private Map<Player, Integer>  playerTimeRemaining;

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
        
        playerTimeRemaining = new HashMap<>();
        playerTimeRemaining.put(white, 3600);
        playerTimeRemaining.put(black, 3600);

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

        System.out.println(piece);

        if(!moves.contains(move))                       throw new InvalidMove(move, moves);
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
        
        Optional<Entry<Player, Integer>> playerTimeout = playerTimeRemaining.entrySet()
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

    public List<Move> getAllPossibleMoves(Position position){

        return moveCache.computeIfAbsent(position, pos ->{
        
            Piece piece = chessModel.getPiece(position);

            List<Move> defaultMoves = piece.defaultMoves(chessModel.getBoard());

            List<Move> allowedMoves = this.chessRules.validateMoves(chessModel, piece, defaultMoves);

            System.out.println(piece);

            System.out.println("Jogadas válidas " + allowedMoves.stream().map(Move::toString).map(s -> s.concat(" ")).reduce(String::concat));

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
        return playerTimeRemaining.get(player);
    }

    public void updateTime(Player player, int time){
        playerTimeRemaining.put(player, time);
    }

    //Retorna DTO do turno
    //Gambiarra, refatorar depois
    public Turn getTurnSummary(Turn previousTurn){

        if(previousTurn == null) {
            return new Turn(
                0, 
                null, 
                black, 
                white, 
                getCurrentPlayer(), 
                0, 
                new HashMap<Player, Integer>(playerTimeRemaining),
                state);
        }

        Integer lastPlayerTime = previousTurn.timeRemaining().get(getCurrentOpponent()) - playerTimeRemaining.get(getCurrentOpponent());

        return new Turn(
            chessModel.getTurn(), 
            chessModel.getLastMove(), 
            black, 
            white,
            getCurrentPlayer(),
            lastPlayerTime,
            new HashMap<Player,Integer>(playerTimeRemaining),
            state
        );
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

    public Player getCurrentOpponent(){
        return chessModel.getCurrentColor() == PlayerColor.BLACK? white : black;
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

        ChessError(String message){
            super(message);

            notifier.notifyError(getLocalizedMessage());

        }
    }

    public class InvalidMove extends ChessError{

        InvalidMove(){
            super();
        }

        InvalidMove(Move move, List<Move> moves){
            super("Jogada " + move.toString() + " não está nas jogadas válidas" + moves.stream().map(Move::toString).map(s -> s.concat(" ")).reduce(String::concat));
        }
    }

    public class NotPlayerTurn extends ChessError{

        NotPlayerTurn(){
            super("Jogada Inválida: Não é o turno desse jogador");
        }
    }

    public class PendingPromotion extends ChessError{

    }

    public class NoPromotionEvent extends ChessError{

    }

    public class GameHasAlreadyEnded extends ChessError{

        GameHasAlreadyEnded(){
            super("Jogada Inválida: Jogo já terminou");
        }
    }

    public class NotPlayerPiece extends ChessError{

    }

}
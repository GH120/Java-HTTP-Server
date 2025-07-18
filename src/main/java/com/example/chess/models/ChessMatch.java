package com.example.chess.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
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
    private Map<Player, Integer>  playerTimeRemaining; //Usar instants e duration depois
    //Campos imutáveis
    private final Player white;
    private final Player black;

    //Componentes públicos: funcionalidades específicas 
    public  final MatchSynchronizer semaphor; //Responsabilidade de sincronização fica fora da partida, expõe métodos (acho melhor tornar isso um observer e colocar um método wait, mas corre o risco de ficar muito complexo)
    public  final TurnHistory       history;

    //Componentes privados: funcionalidades internas
    private final MatchNotifier     notifier; //Meio de comunicação externo para observadores
    private final ChessRules        chessRules;
    private final ChessModel        chessModel;
    private final Map<Position, List<Move>> moveCache;
    
    //Estados de jogo
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
        history    = new TurnHistory();
    }

    ////////////////////////////////////////////////////////////////////
    ///////////////////// INTERFACE PRINCIPAL DE CONTROLES//////////////
    ////////////////////////////////////////////////////////////////////

    //CONTROLES DO JOGADOR: PLAY, SHOWMOVES, CHOOSE PROMOTION, QUIT

    /**Controle para efetuar jogada de Xadrez, solta erros se jogada for inconsistente */
    public void playMove(Player player, Move playedMove) throws ChessError{
        
        Piece piece = chessModel.getPiece(playedMove.origin);

        //Verifica inconsistências na requisição da jogada
        if(state == GameState.CHECKMATE) throw new GameHasAlreadyEnded();
        if(state == GameState.DRAW)      throw new GameHasAlreadyEnded();
        if(state == GameState.EXITED)    throw new GameHasAlreadyEnded();
        if(state == GameState.TIMEOUT)   throw new GameHasAlreadyEnded();
        if(state == GameState.PROMOTION) throw new PendingPromotion();
        if(piece == null)                throw new InvalidMove();
        
        List<Move> moves = getAllPossibleMoves(playedMove.origin);

        //Encontra jogada válida igual a requisitada pelo jogador
        Move move = moves.stream()
                         .filter(m -> m.equals(playedMove))
                         .findFirst()
                         .orElse(null);

        if(move == null)                                throw new InvalidMove(playedMove, moves);
        if(piece.color != chessModel.getCurrentColor()) throw new NotPlayerTurn();
        // if(piece.color != getColor(player))             throw new NotPlayerPiece(); //corrigir bug

        //Uma vez validada, registra jogada no modelo, atualiza estado do jogo e notifica aos observadores
        chessModel.play(piece, move);

        //Problema com relação a promoção, tentar debugar
        updateGameState(move);
        
        notifier.notifyMove(move, chessModel.getCurrentColor());
        
        history.saveTurn();
        
        moveCache.clear();
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

    public void choosePromotion(Pawn.Promotion promotion) throws NoPromotionEvent{

        if(state != GameState.PROMOTION) 
            throw new NoPromotionEvent();

        chessModel.choosePromotion(promotion);

        updateGameState(null);

        //Salva o turno com a promoção escolhida
        //Regra de negócio: turnos com promoção aparecem com id repetido, sobrescrever no database
        history.saveTurn();
    }

    public void quit(){
        state = GameState.EXITED;

        notifier.notifyStateChange(state);
    }

    //////////////////////////////////////////////////////////////
    ///////////////// VERIFICAÇÃO DE ESTADO //////////////////////
    //////////////////////////////////////////////////////////////
    
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

        System.out.println("Evento: " +getState());

        //Depois de avisar se houve cheque ou o jogo acabou, verifica promoção
        if(move != null && move.event == Event.PROMOTION){
            notifier.notifyPromotionRequired(move.destination);

            state = GameState.PROMOTION;
            
            System.out.println("Promoveu?: " + getState());
        }
    }

    // /** Notifica todas as jogadas possíveis para os observers */
    public void showPossibleMoves(Position position){

        notifier.notifyPossibleMoves(getAllPossibleMoves(position));
    }

    //Mover isso para classe interna? => não é controle do jogador, timer usa isso
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


    ////////////////////////////////////////////////////////////////////////
    ////////////////// Getters de utilidade ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
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

    //Mover isso para classe interna?
    public Integer getTime(Player player){
        return playerTimeRemaining.get(player);
    }

    //Mover isso para classe interna?
    public void updateTime(Player player, int time){
        playerTimeRemaining.put(player, time);
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

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////CLASSES INTERNAS: FUNCIONALIDADES AUXILIARES////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    //Usar um countdown latch?
    public class MatchSynchronizer{

        private Player  lastPlayer = null;
        private boolean moveReceived = false;

        public synchronized void waitForMove(Player player) throws InterruptedException {

            while (!moveReceived || lastPlayer.toString().equals(player.toString())) {
                wait(); // Libera o lock até receber notificação
            }

            moveReceived = false; // Reseta para próxima jogada
        }


        //Ambos wait e notifyAll tem que estarem em um bloco synchronized
        public synchronized void notifyMove(Player player) {

            if(player == lastPlayer) return;

            lastPlayer = player;

            moveReceived = true;
            notifyAll(); // Libera as threads bloqueadas
        }
    }


    public class TurnHistory {

        private Stack<Turn> previousTurns = new Stack<Turn>();

        public TurnHistory(){

            //Turno inicial
            previousTurns.add(
                new Turn(
                    0, 
                    null, 
                    black, 
                    white, 
                    getCurrentPlayer(), 
                    0, 
                    new HashMap<Player, Integer>(playerTimeRemaining),
                    chessModel.getCasualties(),
                    state,
                    chessModel.getBoard()
                )
            );
        }

        //Retorna DTO do turno
        //Gambiarra, refatorar depois
        public void saveTurn(){

            Turn previousTurn = previousTurns.peek();

            Integer lastPlayerTime = previousTurn.timeRemaining().get(getCurrentOpponent()) - playerTimeRemaining.get(getCurrentOpponent());

            previousTurns.add(
                new Turn(
                chessModel.getTurn(), 
                chessModel.getLastMove(), 
                black, 
                white,
                getCurrentPlayer(),
                lastPlayerTime,
                new HashMap<Player,Integer>(playerTimeRemaining),
                chessModel.getCasualties(),
                state,
                chessModel.getBoard() //Tem que copiar o tabuleiro atual
            ));
        }

        public Turn lastTurn(){
            return previousTurns.peek();
        }
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////// Erros de Jogada /////////////////////////
    //////////////////////////////////////////////////////////////////
    
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
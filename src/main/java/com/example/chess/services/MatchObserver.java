package com.example.chess.services;

import java.util.List;

import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.chess.models.ChessMatch.GameState;

//TODO: ver como usar isso para avisar os frontends sobre alterações no jogo...
//Será que o server pode registrar o usuário em uma sessão, e usando o ip dele enviar uma requisição post com esse valor?

//Usos prováveis: registrar em database de logging os eventos da partida
//Avisar AI adversária sobre jogadas do jogador
//Detectar fim de jogo ou desistência para desalocar partida do matchManager
//Implementar o timer/sincronizador como um observador? Não, tempo de partida deveria ser uma informação da partida
//Ao mesmo tempo... os dados de tempo poderiam ser da partida, porem o timer poderia ser uma classe a parte que modifica ela
//E o sincronizador também seria uma classe a parte que diria quando uma jogada foi feita
//Essas duas poderiam ser classes internas ao chessMatch, mas que são observadores também (desacoplados da lógica principal)
public interface MatchObserver {

    void onMoveExecuted(Move move, PlayerColor currentPlayer);
    void onGameStateChanged(GameState newState);
    void onPromotionRequired(Position pawnPosition);
    void onShowPossibleMoves(List<Move> moves);
    void onError(String message);
}

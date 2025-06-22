package com.example.chess.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

import com.example.chess.controlers.ChessMatch;
import com.example.chess.controlers.ChessMatchMaker;
import com.example.chess.controlers.ChessMatchManager;
import com.example.chess.controlers.ChessMatch.ChessError;
import com.example.chess.models.Move;
import com.example.chess.models.Piece;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.models.chesspieces.Pawn;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.json.Json;
import com.example.parser.HttpStreamWriter;
import com.fasterxml.jackson.databind.JsonNode;

//Transformar isso num padrão composite, onde cada rota seria um sub controller?
public class ChessAPI {

    //TODO: ENUM? (muito fácil de esquecer de atualizar a lista caso contrário)
    private final List<String>     apiRoute = Arrays.asList(
                                                "/api/findMatch",
                                                "/api/state",
                                                "/api/reset",
                                                "/api/sendMove",
                                                "/api/seeMove",
                                                "/api/getBoard",
                                                "/api/awaitMove"
                                            );

    //Adicionar 
    public void handleRoute(HttpRequest request, InputStream input, OutputStream output) throws ChessError, IOException, InterruptedException{

        System.out.println("API ativada");

        Player player = Player.fromRequest(request);

        switch(request.getPath()){

            case "/api/findMatch" -> {

                //Método assíncrono que espera outro usuário aceitar um duelo
                ChessMatchMaker.getInstance().findDuel(player);

                //Uma vez passada a parte de espera, então encontrou uma partida
                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

                HttpStreamWriter.send(HttpResponse.OK(Json.from(match.getOpponent(player)),null), output);


            }

            case "/api/seeMoves" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Position position = Position.fromRequest(request);

                match.showPossibleMoves(position);

                HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);


            }

            case "/api/ChoosePromotion" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                //Talvez criar uma fábrica de objetos por meio de requests
                Pawn.Promotion promotion = Pawn.PromotionFromRequest(request);

                match.choosePromotion(promotion);

                HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);


            }

            case "/api/sendMove" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

                //Depois criar um move intent que é processado em uma move usando o estado da partida (para movimentos como pawn to e4)
                // Alternativa: MoveIntent interpreta movimentos como "e4" ou "Nf3" usando o estado atual.  
                // Prós: notação natural, resolve ambiguidades. Contras: complexidade extra.  
                // Se necessário: MoveIntent intent = MoveIntent.fromRequest(request); Move move = intent.toMove(match.getBoard())
                Move move = Move.fromRequest(request);

                //Adicionar um try catch
                match.playMove(player, move); //Irá ativar observers, dentre eles o watcher da partida, que irá avisar o outro jogador

                //Adicionar um condicional, pois se houver uma promoção o oponente não será liberado
                match.semaphor.notifyMove();

                //Adicionar resposta padrão sendo um DTO turnSummary que contém o estado do jogo, da jogada e ,se houver promoção, a promoção escolhida. Pensando em retornar tabuleiro como padrão também
                //Será que é bom criar um json mapper no estilo de um visitor? Talvez seja muito complicado e não sei se haveria justificativa
                HttpStreamWriter.send(HttpResponse.OK(move.toJson().getBytes(), "application/json"), output);

            }

            case "/api/awaitMove" -> {
                
                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                match.semaphor.waitForMove();

                //Tem que tratar o caso que o jogo terminou ou que o adversário quitou
                Move move = match.getChessModel().getLastMove();

                HttpStreamWriter.send(HttpResponse.OK(move.toJson().getBytes(), "application/json"), output);

                //Melhor implementação: 
                //Tornar o MatchWatcher em um observador sincronizado, que controla o acesso de ambos os jogadores
                //Quando um estiver esperando, ele fica num estado de espera, e quando ele for acionado pelo outro jogador automaticamente dispara
                //Dessa forma, um jogador não precisa nem saber a existência do outro jogador, apenas o seu watcher, que pode ser mapeado no ChessMatchManager
                //Ele irá guardar o estado da última jogada e todos os efeitos a ela associados, tendo uma função para retornar os eventos da jogada (se foi cheque, se teve en passant...)
                //Usar essa função que retorna um json para montar uma requisição http e mandar de volta para o adversário e para o jogador.

            }

            case "/api/exitMatch" ->{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                match.quit();

                //Tem que avisar o adversário
                //match.semaphor.notifyMove();

                HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);


            }

            //Seria algo auxiliar, mas frontends com noção das regras do jogo poderiam simular jogadas sem precisar disso
            //De qualquer modo, ele garante que o frontend só precisa se preocupar em mostrar o tabuleiro
            case "/api/getBoard" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

                Piece[][] board = match.getChessModel().getBoard();

                HttpStreamWriter.send(HttpResponse.OK(Json.from(board), "application/json"), output);
            }
        }

        // HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);
    }

    public boolean hasEndpoint(String endpoint){
        return apiRoute.contains(endpoint);
    }
}

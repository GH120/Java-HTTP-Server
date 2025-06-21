package com.example.chess.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

import com.example.chess.controlers.ChessMatch;
import com.example.chess.controlers.ChessMatchMaker;
import com.example.chess.controlers.ChessMatchManager;
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


    //Não vai conseguir fazer isso usando observers do jeito tradicional junto com requisições http
    //Problema: Um jogador ao jogar tem de avisar o outro que ele jogou
    //Mas ambos os jogadores estariam esperando uma resposta
    //Essa comunicação teria de ser intermediada pelo servidor
    //Vamos fazer o seguinte: Teremos um estado awaiting que esperará a resposta do outro jogador
    //Quando o outro jogador fizer sua jogada, ele avisará o servidor, que irá retornar a resposta para o primeiro
    //Então no endpoint await, ele iria esperar algum intermediário que rodaria um loop de verificação de estado do modelo, mas isso é custoso
    //Teria alguma forma do java esperar a mudança de estado de um objeto num padrão observer? Sem ele ter que verificar toda hora? E ao desbloquear ele, ele iria proseguir
    //Isso me parece muito com um lock
    public void handleRoute(HttpRequest request, InputStream input, OutputStream output) throws Exception{

        System.out.println("API ativada");

        Player player = Player.fromRequest(request);

        switch(request.getPath()){

            case "/api/findMatch" -> {

                //Criar o MatchWatcher passando a partida e o outputStream
                //Observers são criados no inicio da partida, e serão responsáveis por enviar as mensagens de retorno
                ChessMatchMaker.getInstance().findDuel(player, input, output);

                //Melhor adicionar um lock no find duel 
                // HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);


                break;
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

                match.semaphor.notifyMove();
                
                HttpStreamWriter.send(HttpResponse.OK(move.toJson().getBytes(), "application/json"), output);

            }

            case "/api/awaitMove" -> {
                
                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                match.semaphor.waitForMove();

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

                HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);


            }

            case "/api/getBoard" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

                Piece[][] board = match.getChessModel().getBoard();

                JsonNode node = Json.toJson(board);

                HttpResponse response = HttpResponse.OK(Json.stringify(node).getBytes(), "application/json");

                HttpStreamWriter.send(response, output);
            }
        }

        // HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);
    }

    public boolean hasEndpoint(String endpoint){
        return apiRoute.contains(endpoint);
    }
}

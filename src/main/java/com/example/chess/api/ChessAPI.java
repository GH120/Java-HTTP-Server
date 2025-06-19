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


    public void handleRoute(HttpRequest request, InputStream input, OutputStream output) throws Exception{

        System.out.println("API ativada");

        Player player = Player.fromRequest(request);

        switch(request.getPath()){

            case "/api/findMatch" -> {

                //Criar o MatchWatcher passando a partida e o outputStream
                //Observers são criados no inicio da partida, e serão responsáveis por enviar as mensagens de retorno
                ChessMatchMaker.getInstance().findDuel(player, input, output);

                break;
            }

            case "/api/seeMoves" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Position position = Position.fromRequest(request);

                match.showPossibleMoves(position);

                //Escreve resposta com a lista de movimentos -> Obsever adcionado no começo da partida
                
            }

            case "/api/ChoosePromotion" -> {

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                //Talvez criar uma fábrica de objetos por meio de requests
                Pawn.Promotion promotion = Pawn.PromotionFromRequest(request);

                match.choosePromotion(promotion);

                //Escreve resposta aqui -> adicionar um observer que faz isso

            }

            case "/api/sendMove" -> {


                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);

                //Depois criar um move intent que é processado em uma move usando o estado da partida (para movimentos como pawn to e4)
                Move move = Move.fromRequest(request);

                match.playMove(player, move);
                



                System.out.println(move);
            }

            case "/api/awaitMove" -> {
                Thread.sleep(30000); //Conseguiria a resposta do adversário até então

                HttpStreamWriter.send(HttpResponse.OK(new byte[0], null), output); //Se não retornaria outra resposta OK
            }

            case "/api/exitMatch" ->{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                match.quit();

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

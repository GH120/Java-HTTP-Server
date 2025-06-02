package com.example.chess.api;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

import com.example.chess.controlers.ChessMatch;
import com.example.chess.controlers.ChessMatchMaker;
import com.example.chess.controlers.ChessMatchManager;
import com.example.chess.models.Move;
import com.example.chess.models.Player;
import com.example.chess.models.Position;
import com.example.chess.models.chesspieces.Pawn;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

public class ChessAPI {

    private final List<String>     apiRoute = Arrays.asList(
                                                "/api/findMatch",
                                                "/api/move",
                                                "/api/state",
                                                "/api/reset"
                                            );


    public void handleRoute(HttpRequest request, OutputStream output) throws Exception{

        System.out.println("API ativada");

        Player player = Player.fromRequest(request);

        switch(request.getPath()){

            case "/api/findMatch":{

                //Criar o MatchWatcher passando a partida e o outputStream
                //Observers são criados no inicio da partida, e serão responsáveis por enviar as mensagens de retorno
                ChessMatchMaker.getInstance().findDuel(player, output);

                break;
            }
            case "/api/move":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Move move = Move.fromRequest(request);

                //Transformar isso numa thread
                //Fazer alguma maneira de recuperar a thread do ChessMatchManager
                //Thread teria uma função playMove sincronizada
                match.playMove(player, move);

                //Escreve resposta dizendo que foi um sucesso -> Observer adicionado no começo da partida
                
                break;
            }

            case "/api/seeMoves":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Position position = Position.fromRequest(request);

                match.showPossibleMoves(position);

                //Escreve resposta com a lista de movimentos -> Obsever adcionado no começo da partida
                
                break;
            }

            case "/api/ChoosePromotion":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                //Talvez criar uma fábrica de objetos por meio de requests
                Pawn.Promotion promotion = Pawn.PromotionFromRequest(request);

                match.choosePromotion(promotion);

                //Escreve resposta aqui -> adicionar um observer que faz isso

                break;
            }

            case "/api/exitMatch":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                match.quit();

                break;
            }
        }

        HttpStreamWriter.send(HttpResponse.OK(new byte[0],null), output);
    }

    public boolean hasEndpoint(String endpoint){
        return apiRoute.contains(endpoint);
    }
}

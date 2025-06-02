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

        // request.print();

        Player player = Player.fromRequest(request);

        switch(request.getPath()){

            case "/api/findMatch":{

                //Criar classe HttpClient com o output e input streams
                //Criar o MatchWatcher passando a partida e o outputStream

                ChessMatchMaker.getInstance().findDuel(player);

                break;
            }
            case "/api/move":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Move move = Move.fromRequest(request);

                //Transformar isso numa thread
                //Fazer alguma maneira de recuperar a thread do ChessMatchManager
                //Thread teria uma função playMove sincronizada
                match.playMove(player, move);

                //Escreve resposta dizendo que foi um sucesso -> Adicionar um observer que faz isso
                
                break;
            }

            case "/api/seeMoves":{

                ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
                Position position = Position.fromRequest(request);

                match.showPossibleMoves(position);

                //Escreve resposta com a lista de movimentos -> adicionar um observer que faz isso
                
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

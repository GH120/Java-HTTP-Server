package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.services.ChessMatch.ChessError;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

//Transformar isso num padrão composite, onde cada rota seria um sub controller?
public class ChessAPI extends HttpController{

    ChessAPI(){
        super("/api");
        addController(new FindMatchController("/findMatch"));
        addController(new SendMoveController("/sendMove"));
        addController(new AwaitResponseController("/awaitMove"));
        addController(new ChoosePromotionController("/choosePromotion"));
        addController(new ExitMatchController("/exitMatch"));
        addController(new SeeMovesController("/seeMoves"));
        addController(new GetBoardController("/getBoard"));
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) {
        /**CRUD básico para requisições comuns, requisições de API redirecionadas a ChessAPI*/

        
        try{
            //Se algum dos subcontrollers jogar um erro trata ele
            super.handleRequest(request, input, output);

        }
        catch(ChessError e){ 

            try{
                HttpStreamWriter.send(HttpResponse.BAD_REQUEST(e.getLocalizedMessage().getBytes(), null), output);
            }
            catch(IOException io){
                System.out.println("Envio de erro não funcionou");
                io.printStackTrace();
            }


            e.printStackTrace();
        }
        catch(MatchNotFound e){ 

            try{
                HttpStreamWriter.send(HttpResponse.BAD_REQUEST(e.getLocalizedMessage().getBytes(), null), output);
            }
            catch(IOException io){
                System.out.println("Envio de erro não funcionou");
                io.printStackTrace();
            }


            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Erro na chamada a API");
            e.printStackTrace();
        }

    }
}

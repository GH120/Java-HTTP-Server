package com.example.chess.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.chess.models.ChessMatch;
import com.example.chess.models.Player;
import com.example.chess.models.ChessMatch.NoPromotionEvent;
import com.example.chess.models.chesspieces.Pawn;
import com.example.chess.services.ChessMatchManager;
import com.example.chess.services.ChessMatchManager.MatchNotFound;
import com.example.core.HttpController;
import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStreamWriter;
import com.example.json.Json;
import com.fasterxml.jackson.core.JsonParseException;

public class ChoosePromotionController extends HttpController{

    public ChoosePromotionController(String endpoint) {
        super(endpoint);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws JsonParseException, IOException, MatchNotFound, NoPromotionEvent {
        
        Player player = Player.fromRequest(request);

        ChessMatch match = ChessMatchManager.getInstance().getMatchFromPlayer(player);
                
        //Talvez criar uma fábrica de objetos por meio de requests
        Pawn.Promotion promotion = Pawn.PromotionFromRequest(request);

        match.choosePromotion(promotion);

        HttpStreamWriter.send(HttpResponse.OK(Json.from(match.history.lastTurn()),null), output);
    }

}

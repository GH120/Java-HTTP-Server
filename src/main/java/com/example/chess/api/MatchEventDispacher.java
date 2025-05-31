package com.example.chess.api;

import java.net.http.HttpClient;

import com.example.chess.controlers.MatchObserver;
import com.example.chess.models.Move;
import com.example.chess.models.PieceColor;

//Retorna as respostas http para o cliente sobre os eventos ocorridos na partida
// public class MatchEventDispacher implements MatchObserver{

//     private final HttpClient httpClient;

//         Usar DTO para armazenar objetos a serem serializados para envio 
    
//     @Override
//     public void onMoveExecuted(Move move, PieceColor player) {
//         httpClient.post("/move-events", 
//             new MoveEventDto(move, player));
//     }
    
//     @Override
//     public void onPromotionRequired(Position position) {
//         httpClient.post("/promotion-request", 
//             new PromotionDto(position));
//     }
// }

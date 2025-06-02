// package com.example.chess.controlers;

// import com.example.chess.models.Move;
// import com.example.chess.models.Player;
// import com.example.chess.models.Position;
// import com.example.chess.models.chesspieces.Pawn.Promotion;

// import java.util.ArrayList;
// import java.util.List;

// /**
//  * Classe para testar ChessMatch com partidas já existentes de xadrez.
//  */
// public class ChessMatchSimulator {

//     private final ChessMatch match;
//     private final Player white;
//     private final Player black;

//     public ChessMatchSimulator(Player white, Player black) {
//         this.white = white;
//         this.black = black;
//         this.match = new ChessMatch(white, black);
//     }

//     /**
//      * Executa uma sequência de jogadas no formato simplificado (ex: "e2e4 e7e5 g1f3").
//      */
//     public void simulateMoves(String moveList) {
//         List<Move> moves = parseMoveList(moveList);

//         for (int i = 0; i < moves.size(); i++) {
//             Move move = moves.get(i);
//             Player currentPlayer = (i % 2 == 0) ? white : black;

//             try {
//                 match.playMove(currentPlayer, move);
//                 System.out.println("Move " + (i + 1) + ": " + move);
//             } catch (ChessMatch.ChessError e) {
//                 System.out.println("Erro na jogada " + (i + 1) + ": " + move + " -> " + e.getMessage());
//                 break;
//             }

//             if (match.getState() == ChessMatch.GameState.PROMOTION) {
//                 try {
//                     match.choosePromotion(Promotion.QUEEN); // Default promotion
//                     System.out.println("Promoção automática para Rainha.");
//                 } catch (ChessMatch.NoPromotionEvent e) {
//                     System.out.println("Erro ao tentar promover: " + e.getMessage());
//                 }
//             }
//         }

//         System.out.println("Estado final do jogo: " + match.getState());
//     }

//     /**
//      * Converte uma string como "e2e4 e7e5 g1f3" em uma lista de objetos Move.
//      */
//     private List<Move> parseMoveList(String moveList) {
//         String[] tokens = moveList.trim().split("\\s+");
//         List<Move> moves = new ArrayList<>();

//         for (String token : tokens) {
//             if (token.length() != 4) {
//                 throw new IllegalArgumentException("Formato inválido de jogada: " + token);
//             }
//             Position from = new Position(token.substring(0, 2));
//             Position to = new Position(token.substring(2, 4));
//             moves.add(new Move(from, to));
//         }

//         return moves;
//     }
// }

package com.example.chess.models;

import com.example.chess.models.chesspieces.Pawn;
// import com.example.parser.ChessNotationLexer;
// import com.example.parser.ChessNotationParser;
// import com.example.parser.TreeNode;

public record MoveIntent(Position origin, Position destination, Piece piece, Pawn.Promotion promotion) {

    //Transferir essa responsabilidade para outra classe
    public static MoveIntent fromNotation(String notation){

        Position        origin      = null;
        Position        destination = null;
        Piece           piece       = null;
        Pawn.Promotion  promotion   = null;

        // TreeNode AST = new ChessNotationParser().parse(new ChessNotationLexer().tokenize(notation)).getTree();

        //Extrai parte da AST e coloca nas variáveis

        return new MoveIntent(origin, destination, piece, promotion);
    }
}

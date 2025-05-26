package com.example.chess.models;

import java.util.List;

abstract public class Piece {
    String      type;
    Position    position;
    PieceColor  color;

    abstract public List<Move> allowedMoves(Piece[][] board);
}

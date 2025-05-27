package com.example.chess.models;

public enum PieceColor {
    BLACK, WHITE;

    public PieceColor opposite(){
        return (this == BLACK)? WHITE : BLACK;
    }
}

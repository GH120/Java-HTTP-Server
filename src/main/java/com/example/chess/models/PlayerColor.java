package com.example.chess.models;

public enum PlayerColor {
    BLACK, WHITE;

    public PlayerColor opposite(){
        return (this == BLACK)? WHITE : BLACK;
    }
}

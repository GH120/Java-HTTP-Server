package com.example.chess.api;

import com.example.chess.controlers.ChessMatch.GameState;
import com.example.chess.controlers.MatchObserver;
import com.example.chess.models.Move;
import com.example.chess.models.PlayerColor;
import com.example.chess.models.Position;
import com.example.http.HttpResponse;
import com.example.parser.HttpStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchSynchronizer{

    private boolean moveReceived = false;

    public synchronized void waitForMove() throws InterruptedException {
        while (!moveReceived) {
            wait(); // Libera o lock até receber notificação
        }
        moveReceived = false; // Reseta para próxima jogada
    }


    //Ambos wait e notifyAll tem que estarem em um bloco synchronized
    public synchronized void notifyMove() {
        moveReceived = true;
        notifyAll(); // Libera as threads bloqueadas
    }
}

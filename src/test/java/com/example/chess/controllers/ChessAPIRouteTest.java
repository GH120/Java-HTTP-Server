package com.example.chess.controllers;



import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.core.HttpController;

public class ChessAPIRouteTest {

    @Test
    void shouldRecognizeAllConfiguredRoutes() {
        ChessAPI chessAPI = new ChessAPI();
        
        // Testa os endpoints principais (camelCase)
        assertTrue(chessAPI.hasRoute("/api/findMatch"));
        assertTrue(chessAPI.hasRoute("/api/sendMove"));
        assertTrue(chessAPI.hasRoute("/api/awaitMove"));
        assertTrue(chessAPI.hasRoute("/api/choosePromotion"));
        assertTrue(chessAPI.hasRoute("/api/exitMatch"));
        assertTrue(chessAPI.hasRoute("/api/seeMoves"));
        assertTrue(chessAPI.hasRoute("/api/getBoard"));
        
        // Testa o endpoint base
        assertTrue(chessAPI.hasRoute("/api"));
        
        // Testa caminhos que não existem
        assertFalse(chessAPI.hasRoute("/api/invalidEndpoint"));
        assertFalse(chessAPI.hasRoute("/otherapi/findMatch"));
        assertFalse(chessAPI.hasRoute("/"));
    }

    @Test
    void shouldHandlePathWithTrailingSlash() {
        ChessAPI chessAPI = new ChessAPI();
        
        // Deve reconhecer mesmo com barra no final
        assertTrue(chessAPI.hasRoute("/api/findMatch/"));
        assertTrue(chessAPI.hasRoute("/api/"));
    }

    @Test
    void shouldHandleMixedCasePaths() {
        ChessAPI chessAPI = new ChessAPI();
        
        // Deve ser case-sensitive (camelCase)
        assertFalse(chessAPI.hasRoute("/api/FINDMATCH")); // tudo maiúsculo
        assertFalse(chessAPI.hasRoute("/api/findmatch")); // tudo minúsculo
        assertTrue(chessAPI.hasRoute("/api/findMatch"));  // camelCase correto
    }

    @Test
    void shouldHandleEmptyPath() {
        ChessAPI chessAPI = new ChessAPI();
        assertFalse(chessAPI.hasRoute(""));
    }

    @Test
    void shouldHandleNullPath() {
        ChessAPI chessAPI = new ChessAPI();
        assertFalse(chessAPI.hasRoute(null));
    }
}

// Implementação mínima dos subcontroladores apenas para teste de roteamento
class FindMatchController extends DummyController {
    FindMatchController() { super("/findMatch"); }
}

class SendMoveController extends DummyController {
    SendMoveController() { super("/sendMove"); }
}

class AwaitResponseController extends DummyController {
    AwaitResponseController() { super("/awaitResponse"); }
}

class ChoosePromotionController extends DummyController {
    ChoosePromotionController() { super("/choosePromotion"); }
}

class ExitMatchController extends DummyController {
    ExitMatchController() { super("/exitMatch"); }
}

class SeeMovesController extends DummyController {
    SeeMovesController() { super("/seeMoves"); }
}

class GetBoardController extends DummyController {
    GetBoardController() { super("/getBoard"); }
}

// Classe base dummy para os subcontroladores
abstract class DummyController extends HttpController {
    DummyController(String endpoint) {
        super(endpoint);
    }
    
    @Override
    public boolean hasRoute(String path) {
        // Implementação simples apenas para testar o roteamento
        return path.startsWith(this.endpoint);
    }
}
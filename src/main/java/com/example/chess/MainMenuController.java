package com.example.chess;

public class MainMenuController {

    //Controlaria a opção de entrar em uma partida
    //Ao o usuário buscar uma partida, o servidor procuraria outro usuário para se juntar a ele
    //Loading screen enquanto isso

    private static MainMenuController controller;

    private MainMenuController(){
        
    }
    
    public MainMenuController getInstance(){
        return controller;
    }
}

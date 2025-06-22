package com.example;

import com.example.config.Configuration;
import com.example.config.ConfigurationManager;
import com.example.core.ServerListenerThread;

public class Main {

    public static void main(String[] args) {
        System.out.println("Servidor");

        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/http.json");

        Configuration configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        System.out.println("Using port " + configuration.getPort());
        System.out.println("Using webroot " + configuration.getWebroot());
        
        ServerListenerThread serverListenerThread = new ServerListenerThread(configuration.getPort(), configuration.getWebroot());
        serverListenerThread.start();
    }
}
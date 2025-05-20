package com.example.config;

public class Configuration {

    private int port;
    private String webroot;

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setWebroot(String webroot) {
        this.webroot = webroot;
    }

    public String getWebroot() {
        return webroot;
    }
}

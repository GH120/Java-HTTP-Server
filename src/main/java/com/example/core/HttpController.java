package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.http.HttpRequest;

//Composite pattern para chamadas recursivas a outros controladores de endpoint
public abstract class HttpController implements HttpRouter{

    public  String              endpoint;
    private Map<String,Boolean> routeCache;
    private List<HttpRouter>    subcontrollers;

    public HttpController(String endpoint){
        this.endpoint       = endpoint;
        this.routeCache     = new HashMap<>();
        this.subcontrollers = new ArrayList<>();
    }

    @Override
    public void handleRequest(HttpRequest message, InputStream input, OutputStream output) {
        
        if(!hasRoute(message.getPath())) return;

        HttpRouter chosenRouter = subcontrollers.stream().filter(controller -> controller.hasRoute(message.getPath())).findFirst().get();

        chosenRouter.handleRequest(message,input,output);
    }

    @Override
    public boolean hasRoute(String path) {

        return routeCache.computeIfAbsent(path, value ->{
            
            if(!path.startsWith(endpoint)) return false;
            
            return subcontrollers.stream().anyMatch(controller -> controller.hasRoute(path.substring(0, endpoint.length())));
        });
    }

    public void addController(HttpRouter router){
        this.subcontrollers.add(router);
    }

}

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

        this.routeCache.put(null, false);
    }

    @Override
    public void handleRequest(HttpRequest message, InputStream input, OutputStream output) throws Exception{
        
        if(!hasRoute(message.getPath())) return;

        for(HttpRouter router : subcontrollers){

            if(router.hasRoute(getSubpath(message.getPath()))) {

                router.handleRequest(message,input,output);
                
                break;
            }
        }

    }

    @Override
    public boolean hasRoute(String path) {


        System.out.println(path);

        return routeCache.computeIfAbsent(path, value ->{

            if(path.equals(endpoint))                 return true;
            if(path.equals(endpoint.concat("/"))) return true;
            if(!path.startsWith(endpoint))            return false;
            
            return subcontrollers.stream().anyMatch(controller -> controller.hasRoute(getSubpath(path)));
        });
    }

    public void addController(HttpRouter router){
        this.subcontrollers.add(router);
    }


    //Subtrai o endpoint do caminho 
    public String getSubpath(String path){

        if(endpoint.length() >= path.length()) return null;

        return path.substring(endpoint.length(), path.length());
    }

}

package com.example.core;

import java.io.InputStream;
import java.io.OutputStream;

import com.example.http.HttpRequest;

public abstract class HttpFilter implements HttpRouter {

    private HttpRouter router;

    public HttpFilter(HttpRouter router){
        this.router = router;
    }

    @Override
    public void handleRequest(HttpRequest request, InputStream input, OutputStream output) throws Exception {
       
        request.print();
        //Interage com request e se retornar flag true para sua propagação
        if(intercept(request, input, output)) return;
        
        router.handleRequest(request, input, output);
    }

    @Override
    public boolean hasRoute(String path) {
        return router.hasRoute(path);
    }
    
    abstract protected boolean intercept(HttpRequest request, InputStream input, OutputStream output) throws Exception;
}

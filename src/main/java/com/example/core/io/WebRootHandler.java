package com.example.core.io;

import java.io.File;
import java.io.IOException;

public class WebRootHandler {

    private File webRoot;

    public WebRootHandler(String webRootPath) throws WebRootNotFoundException{
        webRoot = new File(webRootPath);
        if(!webRoot.exists() || !webRoot.isDirectory()){
            throw new WebRootNotFoundException("Webroot provided does not exist");
        }
    }

    //Fazer testes de segurança para garantir que ele não consegue acessar nada fora de webroot
    public File getFile(String relativePath) throws WebRootNotFoundException{

        if(!CheckIfEndsWithSlash(relativePath)) 
            throw new WebRootNotFoundException("Arquivo não encontrado");

        return new File(webRoot, relativePath);


    }

    public boolean CheckIfEndsWithSlash(String relativePath){
        File file = new File(webRoot, relativePath);

        if(!file.exists()) 
            return false;

        try{

            if (file.getCanonicalPath().startsWith(webRoot.getCanonicalPath())) 
                return true;
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return false;


    }
}

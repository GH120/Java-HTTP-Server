package com.example.config;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.example.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class ConfigurationManager {

    private static ConfigurationManager myConfigurationManager;
    private static Configuration myCurrentConfiguration;

    private ConfigurationManager(){

    }

    public static ConfigurationManager getInstance() {
        if(myConfigurationManager == null) 
            myConfigurationManager = new ConfigurationManager();
        return myConfigurationManager;
    }

    public void loadConfigurationFile(String filepath){

        try{

            FileReader filereader = new FileReader(filepath);
            StringBuffer buffer = new StringBuffer();
            
            for(int c = filereader.read(); c != -1; c = filereader.read()){
                buffer.append((char)c);
            }

            JsonNode tree = Json.parse(buffer.toString());
            
            Configuration configuration = new Configuration();

            configuration.setPort(Json.fromJson(tree.get("port"), Integer.class));
            configuration.setWebroot(Json.fromJson(tree.get("webroot"), String.class));
            
            myCurrentConfiguration = configuration;

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public Configuration getCurrentConfiguration(){
        if(myCurrentConfiguration == null){
            throw new HTTPConfigurationException();
        }

        return myCurrentConfiguration;
    }
}


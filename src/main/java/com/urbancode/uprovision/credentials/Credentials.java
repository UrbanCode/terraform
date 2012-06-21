package com.urbancode.uprovision.credentials;

import org.apache.log4j.Logger;

abstract public class Credentials {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(Credentials.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private String name;
    private String username;
    private String password;
    
    //----------------------------------------------------------------------------------------------
    public Credentials(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return username;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return password;
    }
}

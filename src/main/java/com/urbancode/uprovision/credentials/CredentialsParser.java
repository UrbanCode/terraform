package com.urbancode.uprovision.credentials;

import java.util.Properties;

import org.apache.log4j.Logger;

abstract public class CredentialsParser {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(CredentialsParser.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    abstract public Credentials parse(Properties props);
    
}

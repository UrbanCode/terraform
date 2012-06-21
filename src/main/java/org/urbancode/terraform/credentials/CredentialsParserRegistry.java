package org.urbancode.terraform.credentials;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CredentialsParserRegistry {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(CredentialsParserRegistry.class);
    
    static private CredentialsParserRegistry instance = new CredentialsParserRegistry();
    
    //----------------------------------------------------------------------------------------------
    static public CredentialsParserRegistry getInstance() {
        return instance;
    }
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private Map<String, Class<? extends CredentialsParser>> parserMap;
    
    //----------------------------------------------------------------------------------------------
    private CredentialsParserRegistry() {
        parserMap = new HashMap<String, Class<? extends CredentialsParser>>();
    }
    
    //----------------------------------------------------------------------------------------------
    public CredentialsParser getParser(String type) {
        CredentialsParser result = null;
        
        try {
            Class<? extends CredentialsParser> clazz = parserMap.get(type);
            if (clazz == null) {
                throw new NullPointerException("Parser not found for type " + type);
            }
            result = clazz.newInstance();
        } catch (InstantiationException e) {
            log.error("Could not instantiate class " + type + " : " + parserMap.get(type), e);
//            throw e;
        } catch (IllegalAccessException e) {
            log.error("Could not access " + type + " : " + parserMap.get(type), e);
//            throw e;
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public void register(String type, Class<? extends CredentialsParser> clazz) {
        parserMap.put(type, clazz);
    }
}

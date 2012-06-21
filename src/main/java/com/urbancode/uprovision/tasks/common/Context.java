package com.urbancode.uprovision.tasks.common;

import com.urbancode.uprovision.credentials.Credentials;
import com.urbancode.uprovision.credentials.CredentialsException;
import com.urbancode.uprovision.tasks.util.PropertyResolver;


public interface Context {
    
    //----------------------------------------------------------------------------------------------
    public void create();
    
    //----------------------------------------------------------------------------------------------
    public void destroy();
    
    //----------------------------------------------------------------------------------------------
    public void setResolver(PropertyResolver resolver);
    
    //----------------------------------------------------------------------------------------------
    public void setCredentials(Credentials credentials) 
    throws CredentialsException;
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTask getEnvironment() 
    throws Exception;
    
    //----------------------------------------------------------------------------------------------
    public Credentials fetchCredentials();
        
    //----------------------------------------------------------------------------------------------
    public void setProperty(String prop, String value);
    
    //----------------------------------------------------------------------------------------------
    public String resolve(String resolve);
}

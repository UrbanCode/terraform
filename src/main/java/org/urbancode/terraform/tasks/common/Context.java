package org.urbancode.terraform.tasks.common;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.tasks.util.PropertyResolver;


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

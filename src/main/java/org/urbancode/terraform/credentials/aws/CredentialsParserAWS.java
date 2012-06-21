package org.urbancode.terraform.credentials.aws;

import java.util.Properties;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;


public class CredentialsParserAWS extends CredentialsParser {

    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials parse(Properties props) {
        Credentials result = null;
        
        String name = props.getProperty("name");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        
        String accessKey = props.getProperty("access.key");
        String secretKey = props.getProperty("secret.key");
        
        result = new CredentialsAWS(name, username, password, accessKey, secretKey);
        
        return result;
    }

}

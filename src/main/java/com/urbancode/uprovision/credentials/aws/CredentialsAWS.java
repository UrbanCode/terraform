package com.urbancode.uprovision.credentials.aws;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.urbancode.uprovision.credentials.Credentials;

public class CredentialsAWS extends Credentials {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(CredentialsAWS.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private String accessKey;
    private String secretKey;
    private AWSCredentials awsCreds;
    
    //----------------------------------------------------------------------------------------------
    public CredentialsAWS(String name, String username, String password, String accessKey, String secretKey) {
        super(name, username, password);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        awsCreds = null;
    }
    //----------------------------------------------------------------------------------------------
    public String getAccessKey() {
        return accessKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSecretKey() {
        return secretKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public AWSCredentials getBasicAWSCredentials() 
    throws NullPointerException {
        awsCreds = createBasicAWSCredentials();
        
        return awsCreds;
    }
    
    //----------------------------------------------------------------------------------------------
    private AWSCredentials createBasicAWSCredentials() 
    throws NullPointerException {
        AWSCredentials result;
        
        if (accessKey == null || "".equals(accessKey)) {
            log.error("access key for Credentials " + getName() + " is empty!");
            throw new NullPointerException("Empty Access Key");
        }
        
        if (secretKey == null || "".equals(secretKey)) {
            log.error("secret key for Credentials " + getName() + " is empty!");
            throw new NullPointerException("Empty Secret Key");
        }
        
        result = new BasicAWSCredentials(accessKey, secretKey);
        return result;
    }
}

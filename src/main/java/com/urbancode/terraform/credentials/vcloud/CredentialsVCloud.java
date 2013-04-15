package com.urbancode.terraform.credentials.vcloud;

import com.urbancode.terraform.credentials.common.Credentials;

public class CredentialsVCloud extends Credentials {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String organization;
    private String location;
    private String apiKey;
    private String secretKey;

    //----------------------------------------------------------------------------------------------
    public CredentialsVCloud(String name, String username, String password, 
            String organization, String location, String apiKey, String secretKey) {
        super(name, username, password);
        this.organization = organization;
        this.location = location;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getOrganization() {
        return organization;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getLocation() {
        return location;
    }

    //----------------------------------------------------------------------------------------------
    public String getApiKey() {
        return apiKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSecretKey() {
        return secretKey;
    }
}

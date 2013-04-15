package com.urbancode.terraform.credentials.rackspace;

import com.urbancode.terraform.credentials.common.Credentials;

public class CredentialsRackspace extends Credentials {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String apiKey;

    //----------------------------------------------------------------------------------------------
    public CredentialsRackspace(String name, String username, String password, String apiKey) {
        super(name, username, password);
        this.apiKey = apiKey;
    }

    //----------------------------------------------------------------------------------------------
    public String getApiKey() {
        return apiKey;
    }
}

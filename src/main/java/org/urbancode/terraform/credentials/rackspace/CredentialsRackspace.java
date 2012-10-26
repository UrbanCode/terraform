package org.urbancode.terraform.credentials.rackspace;

import org.urbancode.terraform.credentials.Credentials;

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

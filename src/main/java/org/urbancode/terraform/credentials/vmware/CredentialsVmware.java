package org.urbancode.terraform.credentials.vmware;

import org.urbancode.terraform.credentials.Credentials;


public class CredentialsVmware extends Credentials {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String url;

    //----------------------------------------------------------------------------------------------
    public CredentialsVmware(String name, String username, String password, String url) {
        super(name, username, password);
        this.url = url;
    }

    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return url;
    }
}

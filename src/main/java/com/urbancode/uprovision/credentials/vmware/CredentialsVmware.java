package com.urbancode.uprovision.credentials.vmware;

import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.uprovision.credentials.Credentials;
import com.urbancode.uprovision.tasks.vmware.util.Path;

public class CredentialsVmware extends Credentials {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final Logger log = Logger.getLogger(CredentialsVmware.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String url;

    //----------------------------------------------------------------------------------------------
    public CredentialsVmware(String name, String username, String password, String url, List<Path> hosts) {
        super(name, username, password);
        this.url = url;
    }

    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return url;
    }
}

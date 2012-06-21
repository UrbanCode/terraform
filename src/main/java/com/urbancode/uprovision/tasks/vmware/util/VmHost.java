package com.urbancode.uprovision.tasks.vmware.util;

import java.io.Serializable;

public class VmHost implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String url;
    private String user;
    private String password;

    //----------------------------------------------------------------------------------------------
    public VmHost(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return url;
    }

    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return password;
    }
}

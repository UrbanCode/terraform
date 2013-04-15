package com.urbancode.terraform.tasks.rackspace;

public class AuthenticationException extends Exception {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public AuthenticationException() {
    }

    //----------------------------------------------------------------------------------------------
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public AuthenticationException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}

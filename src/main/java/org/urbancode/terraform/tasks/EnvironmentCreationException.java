package org.urbancode.terraform.tasks;

public class EnvironmentCreationException extends Exception {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException() {
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(Throwable cause) {
        super(cause);
    }
}

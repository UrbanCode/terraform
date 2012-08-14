package org.urbancode.terraform.tasks;

public class EnvironmentRestorationException extends Exception {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public EnvironmentRestorationException() {
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentRestorationException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentRestorationException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentRestorationException(Throwable cause) {
        super(cause);
    }
}

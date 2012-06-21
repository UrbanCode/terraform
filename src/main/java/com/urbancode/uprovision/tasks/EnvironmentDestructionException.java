package com.urbancode.uprovision.tasks;

public class EnvironmentDestructionException extends Exception {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public EnvironmentDestructionException() {
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentDestructionException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentDestructionException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentDestructionException(Throwable cause) {
        super(cause);
    }
}

package org.urbancode.terraform.tasks.vmware.util;

public class PoolExhaustedException extends RuntimeException {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public PoolExhaustedException() {
    }

    //----------------------------------------------------------------------------------------------
    public PoolExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public PoolExhaustedException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public PoolExhaustedException(Throwable cause) {
        super(cause);
    }
}

package org.urbancode.terraform.tasks.common.exceptions;

import com.urbancode.x2o.tasks.RestorationException;

public class EnvironmentRestorationException extends RestorationException {

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

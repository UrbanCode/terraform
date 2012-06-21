package com.urbancode.uprovision.tasks.vmware.events;

import com.urbancode.uprovision.tasks.common.SubTask;

public class CloneVmCreatedEvent extends TaskEvent {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************


    //----------------------------------------------------------------------------------------------
    public CloneVmCreatedEvent(SubTask subTask) {
        super(subTask);
    }

}

package org.urbancode.terraform.tasks.vmware.events;

import org.urbancode.terraform.tasks.common.SubTask;

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

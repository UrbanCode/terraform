package org.urbancode.terraform.tasks.vmware.events;

import java.util.EventObject;

import org.urbancode.terraform.tasks.common.SubTask;


public class TaskEvent extends EventObject {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private SubTask subTask;

    //----------------------------------------------------------------------------------------------
    public TaskEvent(SubTask subTask) {
        super(subTask);
        this.subTask = subTask;
    }

    //----------------------------------------------------------------------------------------------
    public SubTask getSubTask() {
        return this.subTask;
    }

}

package org.urbancode.terraform.tasks.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.EnvironmentTask;

public class EnvironmentTaskMicrosoft extends EnvironmentTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(EnvironmentTaskMicrosoft.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<VMTask> vmTasks = new ArrayList<VMTask>();
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskMicrosoft(Context context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    public Context fetchContext() {
        return this.context;
    }

    //----------------------------------------------------------------------------------------------
    public List<VMTask> getVmTasks() {
        return vmTasks;
    }

    //----------------------------------------------------------------------------------------------
    public VMTask createVm() {
        VMTask vmTask = new VMTask(this);
        vmTasks.add(vmTask);
        return vmTask;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        for(VMTask vmTask : vmTasks) {
            try {
                vmTask.create();
            } catch (Exception e) {
                log.warn("Exception while creating Azure VM", e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        for(VMTask vmTask : vmTasks) {
            try {
                vmTask.destroy();
            } catch (Exception e) {
                log.warn("Exception while deleting Azure VM", e);
            }
        }
    }
}

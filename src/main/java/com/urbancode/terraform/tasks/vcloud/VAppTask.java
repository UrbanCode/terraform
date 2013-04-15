package com.urbancode.terraform.tasks.vcloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.x2o.tasks.SubTask;

public class VAppTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VAppTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVCloud env;
    private List<VMTask> vmTasks = new ArrayList<VMTask>();
    
    private String name;
    
    //----------------------------------------------------------------------------------------------
    public VAppTask(EnvironmentTaskVCloud env) {
        super();
        this.env = env;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<VMTask> getVmTasks() {
        return vmTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public VMTask createVmTask() {
        VMTask vmTask = new VMTask();
        vmTasks.add(vmTask);
        return vmTask;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        for (VMTask vmTask : vmTasks) {
            vmTask.create();
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        for (VMTask vmTask : vmTasks) {
            vmTask.destroy();
        }
    }

}

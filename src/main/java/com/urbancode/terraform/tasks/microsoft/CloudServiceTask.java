package com.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;

import com.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;
import com.urbancode.x2o.tasks.SubTask;

public class CloudServiceTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    Logger log = Logger.getLogger(CloudServiceTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    String name;

    //----------------------------------------------------------------------------------------------
    public CloudServiceTask() {
        super();
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
    @Override
    public void create() throws Exception {
        // cloud services are generated when a VM is started
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("service", "delete", name);
    }

}

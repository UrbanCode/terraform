package org.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

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
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("service", "delete", name);
    }

}

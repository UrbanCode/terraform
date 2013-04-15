package com.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;

import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.x2o.tasks.SubTask;

public abstract class SecurityGroupRefTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(VpcSecurityGroupRefTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    protected TerraformContext context;
    protected String groupName;
    protected SecurityGroupTask ref;

    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask(TerraformContext context) {
        this.context = context;
    }

    //----------------------------------------------------------------------------------------------
    public void setSecurityGroupName(String groupName) {
        this.groupName = groupName;
    }

    //----------------------------------------------------------------------------------------------
    public String getSecurityGroupName() {
        return groupName;
    }

    //----------------------------------------------------------------------------------------------
    abstract public SecurityGroupTask fetchSecurityGroup();

}

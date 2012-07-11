package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;

public abstract class SecurityGroupRefTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(VpcSecurityGroupRefTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    protected Context context;
    protected String groupName;
    protected SecurityGroupTask ref; 
    
    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask(Context context) {
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
    // TODO return generic type
    abstract public SecurityGroupTask fetchSecurityGroup();

}

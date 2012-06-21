package com.urbancode.uprovision.tasks.aws;

import org.apache.log4j.Logger;

import com.urbancode.uprovision.tasks.common.SubTask;

public class SecurityGroupRefTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(SecurityGroupRefTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String groupName;
    private VpcSecurityGroupTask ref;
    
    //----------------------------------------------------------------------------------------------
    SecurityGroupRefTask(ContextAWS context) {
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
    
    public VpcSecurityGroupTask fetchSecurityGroup() throws Exception {
        if (ref == null) {
            ref = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSecurityGroupForName(groupName);
        }
        return ref;
    }
    
    //------------------------------------------------------------------((MainEC2)context)----------------------------
    @Override
    public void create() {
        
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        ref = null;
    }
}

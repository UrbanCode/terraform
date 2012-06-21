package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;


public class InstanceRefTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(InstanceRefTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String instanceName;
    private InstanceTask ref;
    
    //----------------------------------------------------------------------------------------------
    InstanceRefTask(ContextAWS context) {
        this.context = context;
    }
    
   //----------------------------------------------------------------------------------------------
    public String getName() {
        return instanceName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String instanceName) {
        this.instanceName = instanceName;
    }
    
    //----------------------------------------------------------------------------------------------
    public InstanceTask fetchInstanceTask() throws Exception {
        if (ref == null) {
            ref = ((EnvironmentTaskAWS)context.getEnvironment()).findInstanceByName(instanceName);
        }
        return ref;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        
    }
    
}

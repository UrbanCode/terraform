package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;

public class Ec2SecurityGroupRefTask extends SecurityGroupRefTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(Ec2SecurityGroupRefTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public Ec2SecurityGroupRefTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public VpcSecurityGroupTask fetchSecurityGroup() {
        if (ref == null) {
            if (context.getEnvironment() instanceof EnvironmentTaskAWS) {
                EnvironmentTaskAWS env = (EnvironmentTaskAWS)context.getEnvironment();
                ref = env.findSecurityGroupByName(groupName);
            }
        }
        
        return ref;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws Exception {
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws Exception {
        
    }
}

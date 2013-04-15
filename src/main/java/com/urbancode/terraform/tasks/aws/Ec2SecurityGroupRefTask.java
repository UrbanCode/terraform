package com.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;

import com.urbancode.terraform.tasks.common.TerraformContext;

public class Ec2SecurityGroupRefTask extends SecurityGroupRefTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(Ec2SecurityGroupRefTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public Ec2SecurityGroupRefTask(TerraformContext context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public SecurityGroupTask fetchSecurityGroup() {
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

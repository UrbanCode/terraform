package org.urbancode.terraform.tasks.vmware;

import org.urbancode.terraform.tasks.common.SubTask;

public class SecurityGroupRefTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String name;
    private SecurityGroupTask securityGroup;

    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return this.name;
    }

    //----------------------------------------------------------------------------------------------
    public SecurityGroupTask fetchSecurityGroup() {
        return this.securityGroup;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setSecurityGroup(SecurityGroupTask securityGroup) {
        this.securityGroup = securityGroup;
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

package com.urbancode.terraform.tasks.vcloud;

import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsException;
import com.urbancode.terraform.credentials.vcloud.CredentialsVCloud;
import com.urbancode.terraform.tasks.common.EnvironmentTask;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.x2o.tasks.CreationException;
import com.urbancode.x2o.tasks.DestructionException;
import com.urbancode.x2o.tasks.RestorationException;

public class ContextVCloud extends TerraformContext {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVCloud env;
    private CredentialsVCloud creds;

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws CreationException {
        env.create();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws DestructionException {
        env.destroy();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws RestorationException {
        //not implemented
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCredentials(Credentials creds)
            throws CredentialsException {
        this.creds = (CredentialsVCloud) creds;
        SavvisClient.getInstance().setCredentials(this.creds);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public EnvironmentTask getEnvironment() {
        return env;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials fetchCredentials() {
        return creds;
    }
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskVCloud createEnvironment() {
        this.env = new EnvironmentTaskVCloud(this);
        return this.env;
    }
    
}

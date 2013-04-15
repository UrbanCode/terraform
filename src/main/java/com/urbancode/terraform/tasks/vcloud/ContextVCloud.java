package com.urbancode.terraform.tasks.vcloud;

import org.apache.log4j.Logger;

import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsException;
import com.urbancode.terraform.credentials.vcloud.CredentialsVCloud;
import com.urbancode.terraform.tasks.common.EnvironmentTask;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.x2o.tasks.CreationException;
import com.urbancode.x2o.tasks.DestructionException;
import com.urbancode.x2o.tasks.RestorationException;
import com.urbancode.x2o.util.PropertyResolver;

public class ContextVCloud implements TerraformContext {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ContextVCloud.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVCloud env;
    private PropertyResolver resolver;
    private CredentialsVCloud creds;
    private SavvisClient client;

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
    public String resolve(String propName) {
        return resolver.resolve(propName);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws RestorationException {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setProperty(String propName, String propValue) {
        resolver.setProperty(propName, propValue);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCredentials(Credentials creds)
            throws CredentialsException {
        this.creds = (CredentialsVCloud) creds;
        this.client = new SavvisClient(this.creds);
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
    public SavvisClient fetchClient() {
        return client;
    }

}

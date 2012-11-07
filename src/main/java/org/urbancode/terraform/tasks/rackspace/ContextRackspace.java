package org.urbancode.terraform.tasks.rackspace;

import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.common.Credentials;
import org.urbancode.terraform.credentials.common.CredentialsException;
import org.urbancode.terraform.credentials.rackspace.CredentialsRackspace;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.x2o.tasks.CreationException;
import com.urbancode.x2o.tasks.DestructionException;
import com.urbancode.x2o.tasks.RestorationException;
import com.urbancode.x2o.util.PropertyResolver;

public class ContextRackspace implements TerraformContext {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ContextRackspace.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskRackspace env;
    private PropertyResolver resolver;
    private CredentialsRackspace creds;
    protected RestClient client;

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws CreationException {
        client = new RestClient();
        try {
            client.authenticate(creds.getUser(), creds.getApiKey());
            env.create();
        } catch (AuthenticationException e) {
            log.error("Authentication failed. Cannot create environment.");
            throw new CreationException(e);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws DestructionException {
        client = new RestClient();
        try {
            client.authenticate(creds.getUser(), creds.getApiKey());
            env.destroy();
        } catch (AuthenticationException e) {
            log.error("Authentication failed. Cannot destroy environment.");
            throw new DestructionException(e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String resolve(String propName) {
        return resolver.resolve(propName);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws RestorationException {
        // TODO Rackspace update commands
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
    public void setCredentials(Credentials credentials)
            throws CredentialsException {
        this.creds = (CredentialsRackspace) credentials;
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
    public EnvironmentTaskRackspace createEnvironment() {
        this.env = new EnvironmentTaskRackspace(this);
        return this.env;
    }

}

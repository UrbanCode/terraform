package org.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.credentials.microsoft.CredentialsMicrosoft;
import org.urbancode.terraform.tasks.common.TerraformContext;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentRestorationException;

import com.urbancode.x2o.util.PropertyResolver;

public class ContextMicrosoft implements TerraformContext {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ContextMicrosoft.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskMicrosoft env;
    private PropertyResolver resolver;
    private CredentialsMicrosoft credentials;

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws EnvironmentCreationException {
        env.create();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws EnvironmentRestorationException {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws EnvironmentDestructionException {
        env.destroy();
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
        this.credentials = (CredentialsMicrosoft) credentials;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public EnvironmentTask getEnvironment() {
        return env;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials fetchCredentials() {
        return credentials;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setProperty(String prop, String value) {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String resolve(String resolve) {
        return resolver.resolve(resolve);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskMicrosoft createEnvironment() {
        env = new EnvironmentTaskMicrosoft(this);
        return env;
    }

}

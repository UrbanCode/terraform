package org.urbancode.terraform.tasks.vmware;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.vmware.CredentialsVmware;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.util.PropertyResolver;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;


public class ContextVmware implements Context {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ContextVmware.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVmware env;
    private PropertyResolver resolver;
    private CredentialsVmware credentials;

    //----------------------------------------------------------------------------------------------
    public ContextVmware() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }

    //----------------------------------------------------------------------------------------------
    public void setProperty(String name, String value) {
        resolver.setProperty(name, value);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCredentials(Credentials credentials) {
        this.credentials = (CredentialsVmware) credentials;
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
    public String resolve(String string) {
        return resolver.resolve(string);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskVmware createEnvironment()
    throws RemoteException, MalformedURLException, IOException {
        env = new EnvironmentTaskVmware(this);
        return env;
    }

    //----------------------------------------------------------------------------------------------
    private VirtualHost createVirtualHost()
    throws IOException {
        VirtualHost result = null;

        if (credentials != null) {
            String url = credentials.getUrl();
            String user = credentials.getUser();
            String password = credentials.getPassword();
            result = new VirtualHost(url, user, password);
        }
        else {
            log.error("Vmware Credentials null!");
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void setVcenterPaths() throws EnvironmentCreationException {
        String datacenter = resolver.resolve("${datacenter}");
        String hostName = resolver.resolve("${host.name}");
        String destination = resolver.resolve("${destination}");
        String datastore = resolver.resolve("${datastore}");

        if("null".equals(datacenter)) {
            throw new EnvironmentCreationException("no datacenter property specified; canceling request");
        }
        if("null".equals(hostName)) {
            throw new EnvironmentCreationException("no host.name property specified; canceling request");
        }
        if("null".equals(destination)) {
            throw new EnvironmentCreationException("no destination property specified; canceling request");
        }
        if("null".equals(datastore)) {
            throw new EnvironmentCreationException("no datastore property specified; canceling request");
        }
        env.setAllPaths(datacenter, hostName, destination, datastore);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {

        VirtualHost host = null;
        try {
            setVcenterPaths();
            host = createVirtualHost();

            env.setVirtualHost(host);
            env.setStartTime(System.currentTimeMillis());
            if (host == null) {
                throw new NullPointerException("Host is null!");
            }
            else {
                env.create();
            }
        }
        catch (IOException e) {
            // TODO - throw
            log.fatal("IOException when trying to run create() on context!", e);
        }
        catch(EnvironmentCreationException e) {
            log.error("a required property was not specified", e);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        VirtualHost host = null;
        try {
            host = createVirtualHost();
        }
        catch (IOException e) {
            // TODO - throw
            log.fatal("IOException when trying to run destroy() on context!", e);
        }
        env.setVirtualHost(host);
        env.destroy();
    }
}

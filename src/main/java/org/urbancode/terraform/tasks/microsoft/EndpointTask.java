package org.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

import com.urbancode.x2o.tasks.SubTask;

public class EndpointTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(EndpointTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String vmName;
    private String publicPort;
    private String privatePort;

    //----------------------------------------------------------------------------------------------
    public EndpointTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getPublicPort() {
        return publicPort;
    }

    //----------------------------------------------------------------------------------------------
    public String getPrivatePort() {
        return privatePort;
    }

    //----------------------------------------------------------------------------------------------
    public void setDnsName(String vmName) {
        this.vmName = vmName;
    }

    //----------------------------------------------------------------------------------------------
    public void setPublicPort(String publicPort) {
        this.publicPort = publicPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setPrivatePort(String privatePort) {
        this.privatePort = privatePort;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("vm", "endpoint", "create", vmName, publicPort, privatePort);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("vm", "endpoint", "delete", vmName, privatePort);
    }

}

package org.urbancode.terraform.tasks.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

public class VMTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VMTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskMicrosoft env;
    private String dnsName;
    private String imageName;
    private String location;
    private String user;
    private String password = "";
    private String roleFileName = "";
    private List<EndpointTask> endpointTasks = new ArrayList<EndpointTask>();

    //----------------------------------------------------------------------------------------------
    public VMTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public VMTask(EnvironmentTaskMicrosoft env) {
        super();
        this.env = env;
    }

    //----------------------------------------------------------------------------------------------
    public String getDnsName() {
        return dnsName;
    }

    //----------------------------------------------------------------------------------------------
    public String getImageName() {
        return imageName;
    }

    //----------------------------------------------------------------------------------------------
    public String getLocation() {
        return location;
    }

    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return password;
    }

    //----------------------------------------------------------------------------------------------
    public String getRoleFileName() {
        return roleFileName;
    }

    //----------------------------------------------------------------------------------------------
    public List<EndpointTask> getEndpointTasks() {
        return endpointTasks;
    }

    //----------------------------------------------------------------------------------------------
    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    //----------------------------------------------------------------------------------------------
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    //----------------------------------------------------------------------------------------------
    public void setLocation(String location) {
        this.location = location;
    }

    //----------------------------------------------------------------------------------------------
    public void setUser(String user) {
        this.user = user;
    }

    //----------------------------------------------------------------------------------------------
    public void setPassword(String password) {
        this.password = password;
    }

    //----------------------------------------------------------------------------------------------
    public void setRoleFileName(String roleFileName) {
        this.roleFileName = roleFileName;
    }

    //----------------------------------------------------------------------------------------------
    public EndpointTask createEndpointTask() {
        EndpointTask task = new EndpointTask();
        task.setDnsName(dnsName);
        endpointTasks.add(task);
        return task;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        if(roleFileName == null || "".equals(roleFileName)) {
            runner.runCommand("vm", "create", dnsName, imageName, user, password, "--location", location);
        }
        else {
            runner.runCommand("vm", "create", dnsName, roleFileName, "--location", location);
        }

        for(EndpointTask task : endpointTasks) {
            task.create();
        }
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
        runner.runCommand("vm", "delete", dnsName);
    }

}

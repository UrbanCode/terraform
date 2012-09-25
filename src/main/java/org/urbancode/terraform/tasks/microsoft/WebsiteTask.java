package org.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

public class WebsiteTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(WebsiteTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    String hostName;
    String location;
    boolean git = false;

    //----------------------------------------------------------------------------------------------
    public WebsiteTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getHostName() {
        return hostName;
    }

    //----------------------------------------------------------------------------------------------
    public String getLocation() {
        return location;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getGit() {
        return git;
    }

    //----------------------------------------------------------------------------------------------
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    //----------------------------------------------------------------------------------------------
    public void setLocation(String location) {
        this.location = location;
    }

    //----------------------------------------------------------------------------------------------
    public void setGit(boolean git) {
        this.git = git;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        if (git) {
            runner.runCommand("vm", "site", "create", hostName, "--location", location, "--git");
        }
        else {
            runner.runCommand("vm", "site", "create", hostName, "--location", location);
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
        log.info("Websites cannot currently be deleted by Terraform. Please delete from web portal.");

    }

}

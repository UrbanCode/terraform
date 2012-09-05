package org.urbancode.terraform.tasks.vmware.chef;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.CloneTask;
import org.urbancode.terraform.tasks.vmware.PostCreateTask;

public class ChefSoloPostCreateTask extends PostCreateTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ChefSoloPostCreateTask.class);
    static protected final String confDirNoSeparator = System.getenv("TERRAFORM_HOME") +
            File.separator + "conf";
    static protected final String confDir = confDirNoSeparator + File.separator;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String cookbookUrl;
    private String soloRbFile = "solo.rb";
    private String nodeJsonFile = "node.json";
    private String jsonConfFile = "chef.json";

    //----------------------------------------------------------------------------------------------
    public ChefSoloPostCreateTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public ChefSoloPostCreateTask(CloneTask cloneTask) {
        super();
        this.cloneTask = cloneTask;
    }

    //----------------------------------------------------------------------------------------------
    public String getCookbookUrl() {
        return this.cookbookUrl;
    }

    //----------------------------------------------------------------------------------------------
    public String getSoloRbFile() {
        return this.soloRbFile;
    }

    //----------------------------------------------------------------------------------------------
    public String getNodeJsonFile() {
        return this.nodeJsonFile;
    }

    //----------------------------------------------------------------------------------------------
    public String getJsonConfFile() {
        return this.jsonConfFile;
    }

    //----------------------------------------------------------------------------------------------
    public void setCookbookUrl(String cookbookUrl) {
        this.cookbookUrl = cookbookUrl;
    }

    //----------------------------------------------------------------------------------------------
    public void setSoloRbFile(String soloRbFile) {
        this.soloRbFile = soloRbFile;
    }

    //----------------------------------------------------------------------------------------------
    public void setNodeJsonFile(String nodeJsonFile) {
        this.nodeJsonFile = nodeJsonFile;
    }

    //----------------------------------------------------------------------------------------------
    public void setJsonConfFile(String jsonConfFile) {
        this.jsonConfFile = jsonConfFile;
    }

    //----------------------------------------------------------------------------------------------
    private void runCookbook()
    throws IOException, InterruptedException {
        try {
            copyFileFromHostToGuest(confDir + "chef-install.sh", "/tmp/install.sh");
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/bash", "/tmp/install.sh");
        }
        catch(IOException e) {
            log.info(e);
            log.info("Chef did not install. Either the command failed or it is already installed.");
        }
        //mkdir returns non-zero exit code if dir already exists, so we ignore failures
        mkdirIgnoreExceptions("/etc/chef");
        mkdirIgnoreExceptions("/var/chef");
        mkdirIgnoreExceptions("/var/chef/data_bags");

        copyFileFromHostToGuest(confDir + soloRbFile, "/etc/chef/solo.rb");
        copyFileFromHostToGuest(confDir + nodeJsonFile, "/etc/chef/node.json");
        copyFileFromHostToGuest(confDir + jsonConfFile, "/etc/chef/" + jsonConfFile);
        //attempts to run the cookbook and pipes the output to log.out in /var/chef on the vm
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/usr/bin/chef-solo", "-r", cookbookUrl,
                "-j", "/etc/chef/" + jsonConfFile, "-L", "/var/chef/chef.out");
    }

    //----------------------------------------------------------------------------------------------
    private void mkdirIgnoreExceptions(String dir) {
        try {
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/mkdir", dir);
        } catch (IOException e) {
            //swallow
        } catch (InterruptedException e) {
            //swallow
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        this.context = environment.fetchContext();
        this.vmToConfig = cloneTask.fetchVm();

        try {
            cloneTask.powerOnVm();
            environment.fetchVirtualHost().waitForIp(vmToConfig);
            runCookbook();
        }
        catch (IOException e) {
            log.warn("IOException while running Chef Solo cookbook. A VMRun command probably failed." +
            		"Check /var/chef/chef.out on your guest VM, if it exists.", e);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException while running Chef Solo cookbook.", e);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {

    }
}

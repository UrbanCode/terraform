package org.urbancode.terraform.tasks.microsoft;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

import com.urbancode.x2o.tasks.SubTask;

public class DiskAttachTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(DiskAttachTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String diskName;
    private String vmName;

    //----------------------------------------------------------------------------------------------
    public DiskAttachTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getDiskName() {
        return diskName;
    }

    //----------------------------------------------------------------------------------------------
    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    //----------------------------------------------------------------------------------------------
    public void setDnsName(String vmName) {
        this.vmName = vmName;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("vm", "disk", "attach", vmName, diskName);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub

    }

}

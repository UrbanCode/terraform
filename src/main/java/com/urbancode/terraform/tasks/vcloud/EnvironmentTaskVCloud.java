package com.urbancode.terraform.tasks.vcloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.terraform.tasks.common.EnvironmentTask;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentRestorationException;

public class EnvironmentTaskVCloud extends EnvironmentTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(EnvironmentTaskVCloud.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<VAppTask> vAppTasks = new ArrayList<VAppTask>();
    
    private String vdcId;
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskVCloud(TerraformContext context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public List<VAppTask> getVAppTasks() {
        return vAppTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getVcdId() {
        return vdcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVcdId(String vdcId) {
        this.vdcId = vdcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public VAppTask createVApp() {
        VAppTask vApp = new VAppTask(this);
        vAppTasks.add(vApp);
        return vApp;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws EnvironmentCreationException {
        for (VAppTask vAppTask : vAppTasks) {
            try {
                vAppTask.create();
            } catch (Exception e) {
                log.error("Exception while creating vCloud environment", e);
                throw new EnvironmentCreationException(e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws EnvironmentRestorationException {
        // TODO Auto-generated method stub
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws EnvironmentDestructionException {
        for (VAppTask vAppTask : vAppTasks) {
            try {
                vAppTask.destroy();
            } catch (Exception e) {
                log.error("Exception while destroying vCloud environment", e);
                throw new EnvironmentDestructionException(e);
            }
        }
    }
}

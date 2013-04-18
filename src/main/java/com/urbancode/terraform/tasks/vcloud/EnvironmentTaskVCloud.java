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
    static final private int MAX_THREADS = 30;

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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

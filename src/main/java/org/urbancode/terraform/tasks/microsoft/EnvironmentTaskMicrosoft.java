package org.urbancode.terraform.tasks.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.TerraformContext;
import org.urbancode.terraform.tasks.common.EnvironmentTask;

public class EnvironmentTaskMicrosoft extends EnvironmentTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(EnvironmentTaskMicrosoft.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<VMTask> vmTasks = new ArrayList<VMTask>();
    private List<WebsiteTask> websiteTasks = new ArrayList<WebsiteTask>();
    private List<CloudServiceTask> csTasks = new ArrayList<CloudServiceTask>();
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskMicrosoft(TerraformContext context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    public TerraformContext fetchContext() {
        return (TerraformContext) this.context;
    }

    //----------------------------------------------------------------------------------------------
    public List<VMTask> getVmTasks() {
        return vmTasks;
    }

    //----------------------------------------------------------------------------------------------
    public List<WebsiteTask> getWebsiteTasks(){
        return websiteTasks;
    }

    //----------------------------------------------------------------------------------------------
    public List<CloudServiceTask> getCloudServiceTasks(){
        return csTasks;
    }

    //----------------------------------------------------------------------------------------------
    public VMTask createVm() {
        VMTask vmTask = new VMTask();
        vmTasks.add(vmTask);
        return vmTask;
    }

    //----------------------------------------------------------------------------------------------
    public WebsiteTask createWebsite() {
        WebsiteTask websiteTask = new WebsiteTask();
        websiteTasks.add(websiteTask);
        return websiteTask;
    }

    //----------------------------------------------------------------------------------------------
    public CloudServiceTask createCloudService() {
        CloudServiceTask csTask = new CloudServiceTask();
        csTasks.add(csTask);
        return csTask;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        for(VMTask vmTask : vmTasks) {
            try {
                vmTask.setUuid(uuid);
                vmTask.create();
                //CloudServiceTask csTask = createCloudService();
                //csTask.setName(vmTask.getDnsName());
            } catch (Exception e) {
                log.warn("Exception while creating Azure VM", e);
            }
        }

        for(WebsiteTask site : websiteTasks) {
            try {
                site.create();
            }
            catch (Exception e) {
                log.warn("Exception while creating Azure website", e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        for(VMTask vmTask : vmTasks) {
            try {
                vmTask.destroy();
            } catch (Exception e) {
                log.warn("Exception while deleting Azure VM", e);
            }
        }

        for(WebsiteTask site : websiteTasks) {
            try {
                site.destroy();
            }
            catch (Exception e) {
                log.warn("Exception while creating Azure website", e);
            }
        }
    }
}

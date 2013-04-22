package com.urbancode.terraform.tasks.vcloud;

import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.x2o.tasks.SubTask;

public class VMTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VMTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private NetworkTask networkTask;
    private List<DiskTask> diskTasks;
    
    private String name;
    private String href;
    
    
    //----------------------------------------------------------------------------------------------
    public VMTask() {
        super();
    }
    
    //----------------------------------------------------------------------------------------------
    public NetworkTask getNetworkTask() {
        return networkTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<DiskTask> getDiskTasks() {
        return diskTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public NetworkTask createNetworkTask() {
        this.networkTask = new NetworkTask();
        return this.networkTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getHref() {
        return href;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setHref(String href) {
        this.href = href;
    }
    
    //----------------------------------------------------------------------------------------------
    public DiskTask createDiskTask() {
        DiskTask diskTask = new DiskTask();
        diskTasks.add(diskTask);
        return diskTask;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub

    }

}

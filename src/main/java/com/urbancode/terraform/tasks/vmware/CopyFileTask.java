package com.urbancode.terraform.tasks.vmware;

import java.io.IOException;

import org.apache.log4j.Logger;

public class CopyFileTask extends PostCreateTask {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(CopyFileTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String origin;
    private String destination;
    
    //----------------------------------------------------------------------------------------------
    public CopyFileTask(){
        super();
    }
    
    //----------------------------------------------------------------------------------------------
    public CopyFileTask(CloneTask cloneTask) {
        super(cloneTask);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getOrigin() {
        return origin;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getDestination() {
        return destination;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        this.context = environment.fetchContext();
        this.vmToConfig = cloneTask.fetchVm();

        try {
            copyFileFromHostToGuest(origin, destination);
        } catch (IOException e) {
            log.warn("problem while copying file from host to guest", e);
        } catch (InterruptedException e) {
            log.warn("problem while copying file from host to guest", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
    }
}

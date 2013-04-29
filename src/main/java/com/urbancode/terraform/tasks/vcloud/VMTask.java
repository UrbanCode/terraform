package com.urbancode.terraform.tasks.vcloud;

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
    
    private String name;
    private String href;
    
    
    //----------------------------------------------------------------------------------------------
    public VMTask() {
        super();
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

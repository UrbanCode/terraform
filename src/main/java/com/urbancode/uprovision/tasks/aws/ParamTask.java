package com.urbancode.uprovision.tasks.aws;

import org.apache.log4j.Logger;

import com.urbancode.uprovision.tasks.common.Context;
import com.urbancode.uprovision.tasks.common.SubTask;

public class ParamTask extends SubTask {
    

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(ParamTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

//    private ContextEC2 context;
    
    private String value;
    
    //----------------------------------------------------------------------------------------------
    public ParamTask(ContextAWS context) {
//        this.context = context;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setValue(String value) {
        this.value = value;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getValue() {
        return value;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        
    }
}

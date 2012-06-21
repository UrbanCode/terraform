package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;


public abstract class PostCreateSubTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PostCreateSubTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    protected String script;
    
    //----------------------------------------------------------------------------------------------
    protected PostCreateSubTask() {
        
    }
    
    //----------------------------------------------------------------------------------------------
    protected PostCreateSubTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public abstract void setCmds(String script);
    
    //----------------------------------------------------------------------------------------------
    protected abstract String getCmds();
    
    //----------------------------------------------------------------------------------------------
    @Override
    abstract public void create();

    //----------------------------------------------------------------------------------------------
    @Override
    abstract public void destroy();
}

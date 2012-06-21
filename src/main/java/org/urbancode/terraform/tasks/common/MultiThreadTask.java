package org.urbancode.terraform.tasks.common;

import java.util.ArrayList;
import java.util.List;


public class MultiThreadTask extends Task implements Runnable {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private Task task;
    private boolean doCreate;
    private List<Exception> exceptions;
    
    //----------------------------------------------------------------------------------------------
    public MultiThreadTask(Task task, boolean doCreate, Context context) {
        super(context);
        this.task = task;
        this.doCreate = doCreate;
        exceptions = new ArrayList<Exception>();
    }
    
    //----------------------------------------------------------------------------------------------
    public MultiThreadTask(Context context) {
        super(context);
        exceptions = new ArrayList<Exception>();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void run() {
        try {
            if (doCreate) {
                task.create();
            }
            else {
                task.destroy();
            }
        }
        catch (Exception e) {
            exceptions.add(e);
        }
    }
    
    //----------------------------------------------------------------------------------------------
    public List<Exception> getExceptions() {
        return exceptions;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        // TODO Auto-generated method stub
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}

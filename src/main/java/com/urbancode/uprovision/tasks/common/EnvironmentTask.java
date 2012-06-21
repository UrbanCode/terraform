package com.urbancode.uprovision.tasks.common;


public class EnvironmentTask extends Task {
    
    private String name;
    private long startTime;
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTask() {
        super(null);
    }
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public long getStartTime() {
        return startTime;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
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

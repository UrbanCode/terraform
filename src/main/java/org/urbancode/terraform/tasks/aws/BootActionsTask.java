package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;


public class BootActionsTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(BootActionsTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String shell;
    private String userData;
    private List<PostCreateSubTask> actions;

    //----------------------------------------------------------------------------------------------
    public BootActionsTask(ContextAWS context) {
        this.context = context;
        actions = new ArrayList<PostCreateSubTask>();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setShell(String shell) {
        this.shell = shell;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setUserData(String userData) {
        this.userData = userData;
    }
    
    //----------------------------------------------------------------------------------------------
    protected String getUserData() {
        return userData;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<PostCreateSubTask> getScript() {
        return Collections.unmodifiableList(actions);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getShell() {
        return shell;
    }
    
    //----------------------------------------------------------------------------------------------
    public ScriptTask createScript() {
        PostCreateSubTask scriptTask = new ScriptTask(context);
        actions.add(scriptTask);
        return (ScriptTask)scriptTask;
    }
    
    // TODO - not tested if works with puppetTasks AND scriptTasks.
    //         You should be able to just complete teh puppet task 
    //         with the scriptTask.
    //----------------------------------------------------------------------------------------------
    public PostCreateSubTask createPuppet() {
        PostCreateSubTask puppet = new PuppetTask(context);
        actions.add(puppet);
        return puppet;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        String userData = "";
        userData += "#!" + shell + " \n\n";  // #! is REQUIRED to be first characters)
        
        setUserData(userData);
        if (getScript() != null) {
            for (PostCreateSubTask task : getScript()) {
                task.create();
                setUserData(getUserData() + task.getCmds());
            }
            setUserData(context.resolve(getUserData()));
        }
        log.info("user-data: \n\n" + getUserData());
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        setUserData(null);
        //already disconnected
    }

}

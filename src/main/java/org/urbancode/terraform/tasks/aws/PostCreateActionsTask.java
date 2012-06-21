package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;


public class PostCreateActionsTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PostCreateActionsTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private List<SshTask> actions = new ArrayList<SshTask>();
    private String host;
    private String idFilePath;
    
    //----------------------------------------------------------------------------------------------
    public PostCreateActionsTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public void setHost(String host) {
        this.host = host;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setIdFile(String idFilePath) {
        this.idFilePath = idFilePath;
    }

    //----------------------------------------------------------------------------------------------
    public List<SshTask> getPostCreateActions() {
        return Collections.unmodifiableList(actions);
    }
    
    //----------------------------------------------------------------------------------------------
    public SshTask createSsh() {
        SshTask tmp = new SshTask(context);
        actions.add(tmp);
        return tmp;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws Exception {
        
        if (actions != null) {
            for (SshTask action : actions) {
                if (idFilePath != null) {
                    action.setIdFilePath(idFilePath);
                }
                
                if (host != null) {
                    action.setHost(host);
                    action.create();
                }
                else {
                    log.error("Host is null!" +
                              "\nNo where to connect to do PostCreateActions!" +
                              "\nSkipping PCAs");
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws Exception {
        // TODO - anything here? 
    }
}

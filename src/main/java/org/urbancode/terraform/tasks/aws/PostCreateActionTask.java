package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;

/**
 * This class holds all info for creating a connection to a server which will be necessary if you'd
 * like to run some actions on it after it has started.
 * 
 * @author ncc
 *
 */
public abstract class PostCreateActionTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PostCreateActionTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    protected String host;
    protected String idFilePath;
    
    protected int port = 22;  // default ssh port
    protected String user;
    protected String pass;
    
    public PostCreateActionTask(Context context) {
        super(context);
    }
    
    public PostCreateActionTask() {
        super();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setUser(String user) {
        this.user = user;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPassword(String pass) {
        this.pass = pass;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setHost(String host) {
        this.host = host;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPort(int port) {
        this.port = port;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setIdFilePath(String idFilePath) {
        this.idFilePath = idFilePath;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return pass;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getHost() {
        return host;
    }
    
    //----------------------------------------------------------------------------------------------
    public int getPort() {
        return port;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getIdFilePath() {
        return idFilePath;
    }
    
}

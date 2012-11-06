package org.urbancode.terraform.tasks.rackspace;

import org.apache.log4j.Logger;

import com.urbancode.x2o.tasks.SubTask;

public class DatabaseUserTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(DatabaseUserTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    String username;
    String password;

    //----------------------------------------------------------------------------------------------
    public DatabaseUserTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getUsername() {
        return username;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return "${" + username + ".password}";
    }

    //----------------------------------------------------------------------------------------------
    public String fetchPassword() {
        return password;
    }

    //----------------------------------------------------------------------------------------------
    public void setUsername(String username) {
        this.username = username;
    }

    //----------------------------------------------------------------------------------------------
    public void setPassword(String password) {
        this.password = password;
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

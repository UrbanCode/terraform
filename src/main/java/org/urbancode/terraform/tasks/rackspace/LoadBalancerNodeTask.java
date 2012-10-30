package org.urbancode.terraform.tasks.rackspace;

import org.apache.log4j.Logger;

import com.urbancode.x2o.tasks.SubTask;

public class LoadBalancerNodeTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(LoadBalancerNodeTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    String address;
    String port;
    String condition = "ENABLED";

    //----------------------------------------------------------------------------------------------
    public LoadBalancerNodeTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getAddress() {
        return address;
    }

    //----------------------------------------------------------------------------------------------
    public String getPort() {
        return port;
    }

    //----------------------------------------------------------------------------------------------
    public String getCondition() {
        return condition;
    }

    //----------------------------------------------------------------------------------------------
    public void setAddress(String address) {
        this.address = address;
    }

    //----------------------------------------------------------------------------------------------
    public void setPort(String port) {
        this.port = port;
    }

    //----------------------------------------------------------------------------------------------
    public void setCondition(String condition) {
        this.condition = condition;
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

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

}

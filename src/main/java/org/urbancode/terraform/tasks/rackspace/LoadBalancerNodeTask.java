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
    private EnvironmentTaskRackspace env;
    private String address;
    private String port;
    private String condition = "ENABLED";
    private String serverName = null;
    private boolean nameHasSuffix = false;
    private final String ipType;

    //----------------------------------------------------------------------------------------------
    public LoadBalancerNodeTask(EnvironmentTaskRackspace env, String ipType) {
        super();
        this.env = env;
        this.ipType = ipType;
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
    public String getServerName() {
        return serverName;
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
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    //----------------------------------------------------------------------------------------------
    public void setNameHasSuffix(boolean nameHasSuffix) {
        this.nameHasSuffix = nameHasSuffix;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        if (serverName != null) {
            if (nameHasSuffix) {
                serverName = serverName + "-" + env.fetchSuffix();
            }
            if (ipType.equalsIgnoreCase("private")) {
                address = env.fetchContext().resolve("${" + serverName + "-private-ip" + "}");
            }
            else if (ipType.equalsIgnoreCase("public")) {
                address = env.fetchContext().resolve("${" + serverName + "-public-ip" + "}");
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        // Load balancer nodes are deleted when the load balancer is deleted
    }

}

package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;


public class ListenerTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(ListenerTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
//    private ContextEC2 context;
    
    private String protocol;
    private String certId;
    private int instancePort;
    private int loadBalancerPort;
    
    //----------------------------------------------------------------------------------------------
    ListenerTask(ContextAWS context) {
//        this.context = context;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setProtocol(String protocol) {
        // TODO - check for valid protocol
        this.protocol = protocol;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCertId(String certId) {
        this.certId = certId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setInstancePort(int instancePort) {
        this.instancePort = instancePort;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setLoadBalancerPort(int loadBalancerPort) {
        this.loadBalancerPort = loadBalancerPort;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getProtocol() {
        return protocol;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getCertId() {
        return certId;
    }
    
    //----------------------------------------------------------------------------------------------
    public int getInstancePort() {
        return instancePort;
    }
    
    //----------------------------------------------------------------------------------------------
    public int getLoadBalancerPort() {
        return loadBalancerPort;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean isSecure() {
        boolean result = false;
        if (getProtocol().equalsIgnoreCase("https") || getProtocol().equalsIgnoreCase("ssl")) {
            result = true;
        }
        return result;
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

/*******************************************************************************
 * Copyright 2012 Urbancode, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
    public ListenerTask(ContextAWS context) {
        super(context);
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

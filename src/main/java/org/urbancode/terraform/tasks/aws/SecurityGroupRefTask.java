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
import org.urbancode.terraform.tasks.common.SubTask;


public class SecurityGroupRefTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(SecurityGroupRefTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String groupName;
    private VpcSecurityGroupTask ref;
    
    //----------------------------------------------------------------------------------------------
    SecurityGroupRefTask(ContextAWS context) {
        this.context = context;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setSecurityGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSecurityGroupName() {
        return groupName;
    }
    
    public VpcSecurityGroupTask fetchSecurityGroup() throws Exception {
        if (ref == null) {
            ref = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSecurityGroupForName(groupName);
        }
        return ref;
    }
    
    //------------------------------------------------------------------((MainEC2)context)----------------------------
    @Override
    public void create() {
        
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        ref = null;
    }
}

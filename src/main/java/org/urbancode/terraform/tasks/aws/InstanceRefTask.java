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

import com.urbancode.x2o.tasks.SubTask;


public class InstanceRefTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(InstanceRefTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private ContextAWS context;

    private String instanceName;
    private InstanceTask ref;

    //----------------------------------------------------------------------------------------------
    public InstanceRefTask(ContextAWS context) {
        this.context = context;
    }

   //----------------------------------------------------------------------------------------------
    public String getName() {
        return instanceName;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String instanceName) {
        this.instanceName = instanceName;
    }

    //----------------------------------------------------------------------------------------------
    public InstanceTask fetchInstanceTask() throws Exception {
        if (ref == null) {
            ref = ((EnvironmentTaskAWS)context.getEnvironment()).findInstanceByName(instanceName);
        }
        return ref;
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

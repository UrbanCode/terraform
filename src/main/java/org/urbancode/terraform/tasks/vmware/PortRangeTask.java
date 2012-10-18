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
package org.urbancode.terraform.tasks.vmware;

import com.urbancode.x2o.tasks.SubTask;


public class PortRangeTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private int firstPort;
    private int lastPort;

    //----------------------------------------------------------------------------------------------
    public PortRangeTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public PortRangeTask(int firstPort, int lastPort) {
        super();
        this.firstPort = firstPort;
        this.lastPort = lastPort;
    }

    //----------------------------------------------------------------------------------------------
    public int getFirstPort() {
        return firstPort;
    }

    //----------------------------------------------------------------------------------------------
    public int getLastPort() {
        return lastPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setFirstPort(int firstPort) {
        this.firstPort = firstPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setLastPort(int lastPort) {
        if(lastPort > this.firstPort) {
            this.lastPort = lastPort;
        }
        else {
            this.lastPort = this.firstPort;
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        //port range rules are applied via vmrun commands in post create tasks
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        //port range rules are applied via vmrun commands in post create tasks
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {
        //port range rules are applied via vmrun commands in post create tasks
    }

}

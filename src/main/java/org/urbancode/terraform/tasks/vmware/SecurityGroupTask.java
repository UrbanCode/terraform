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

import java.util.ArrayList;
import java.util.List;

import org.urbancode.terraform.tasks.common.SubTask;


public class SecurityGroupTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String name;
    private List<PortRangeTask> portRanges = new ArrayList<PortRangeTask>();

    //----------------------------------------------------------------------------------------------
    public SecurityGroupTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return this.name;
    }

    //----------------------------------------------------------------------------------------------
    public List<PortRangeTask> getPortRanges() {
        return this.portRanges;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public PortRangeTask createPortRange() {
        PortRangeTask result = new PortRangeTask();
        portRanges.add(result);
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

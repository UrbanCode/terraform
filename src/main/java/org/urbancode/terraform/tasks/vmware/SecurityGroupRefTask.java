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

import org.urbancode.terraform.tasks.common.SubTask;

public class SecurityGroupRefTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String name;
    private SecurityGroupTask securityGroup;

    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return this.name;
    }

    //----------------------------------------------------------------------------------------------
    public SecurityGroupTask fetchSecurityGroup() {
        return this.securityGroup;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setSecurityGroup(SecurityGroupTask securityGroup) {
        this.securityGroup = securityGroup;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        //security group rules are applied via vmrun commands in post create tasks
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        //security group rules are applied via vmrun commands in post create tasks
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {
        //security group rules are applied via vmrun commands in post create tasks
    }

}

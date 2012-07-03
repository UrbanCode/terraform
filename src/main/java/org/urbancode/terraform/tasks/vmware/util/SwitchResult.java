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
package org.urbancode.terraform.tasks.vmware.util;

import java.io.Serializable;

import com.vmware.vim25.mo.Network;

public class SwitchResult implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private Network network;
    final private Path switchPath;
    final private Path portGroupPath;

    //----------------------------------------------------------------------------------------------
    public SwitchResult(Network network, Path switchPath, Path portGroupPath) {
        if (network == null) {
            throw new NullPointerException("Network is null in SwitchResult");
        }
        if (switchPath == null) {
            throw new NullPointerException("switchPath is null in SwitchResult");
        }
        if (portGroupPath == null) {
            throw new NullPointerException("portGroupPath is null in SwitchResult");
        } 
        
        this.network = network;
        this.switchPath = switchPath;
        this.portGroupPath = portGroupPath;
    }

    //----------------------------------------------------------------------------------------------
    public Network getNetwork() {
        return network;
    }

    //----------------------------------------------------------------------------------------------
    public Path getSwitchPath() {
        return switchPath;
    }
    //----------------------------------------------------------------------------------------------
    public Path getPortGroupPath() {
        return portGroupPath;
    }
}

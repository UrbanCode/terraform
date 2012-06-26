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

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.util.Ip4;
import org.urbancode.terraform.tasks.vmware.util.IpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.IpInUseException;


public class GlobalIpAddressPool {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(GlobalIpAddressPool.class);
    private static GlobalIpAddressPool instance = new GlobalIpAddressPool();

    //----------------------------------------------------------------------------------------------
    public static GlobalIpAddressPool getInstance() {
        return instance;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private IpAddressPool addressPool = null;

    //----------------------------------------------------------------------------------------------
    private GlobalIpAddressPool() {
        if (addressPool == null) {
            addressPool = new IpAddressPool("10.15.50.1", "10.15.50.250");
        }
    }

    //----------------------------------------------------------------------------------------------
    public IpAddressPool getIpAddressPool() {
        return addressPool;
    }

    //----------------------------------------------------------------------------------------------
    synchronized public void reserveIp(Ip4 ip)
    throws IpInUseException {
        addressPool.reserveIp(ip);
    }

    //----------------------------------------------------------------------------------------------
    synchronized public Ip4 allocateIp() {
        Ip4 result = addressPool.allocateIp();
        return result;
    }

    //----------------------------------------------------------------------------------------------
    synchronized public void releaseIp(Ip4 ip) {
        addressPool.releaseIp(ip);
    }
}

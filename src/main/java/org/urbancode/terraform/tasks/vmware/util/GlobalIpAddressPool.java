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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

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
        Properties poolConfig = parseIpPoolFile();
        addressPool = createIpPoolFromProps(poolConfig);
    }
    
    //----------------------------------------------------------------------------------------------
    private IpAddressPool createIpPoolFromProps(Properties props) {
        
        if (props == null) {
            // fallback to defaults if we have null props
            props = new Properties();
        }
        
        String start = props.getProperty("start", "10.15.50.1");
        log.info("IpAddressPool start: " + start);
        
        String end = props.getProperty("end", "10.15.50.250");
        log.info("IpAddressPool end: " + end);
        
        return new IpAddressPool(start, end);
    }

    //----------------------------------------------------------------------------------------------
    private Properties parseIpPoolFile() {
        Properties result = new Properties();
        
        // TODO - make this file configurable?
        String ipPoolFilePath = System.getProperty("user.home") + File.separator + ".terraform" + File.separator + "ippool.conf";
        
        File poolFile = new File(ipPoolFilePath); 
        
        InputStream in = null;
        try {
            in = new FileInputStream(poolFile);
            result.load(in);
        } 
        catch (FileNotFoundException e) {
            log.fatal("Could not find file " + ipPoolFilePath);
//            throw new FileNotFoundException("Unable to find file: " + ipPoolFilePath);
        }
        catch (IOException e) {
            log.fatal("Could not read properties from " + ipPoolFilePath);
//            throw new IOException("Unable to read properties from: " + ipPoolFilePath);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // swallow
                }
            }
        }
        
        return result;
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

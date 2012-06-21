package com.urbancode.uprovision.tasks.vmware.util;

import org.apache.log4j.Logger;

import com.urbancode.uprovision.tasks.vmware.util.Ip4;
import com.urbancode.uprovision.tasks.vmware.util.IpAddressPool;
import com.urbancode.uprovision.tasks.vmware.util.IpInUseException;

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

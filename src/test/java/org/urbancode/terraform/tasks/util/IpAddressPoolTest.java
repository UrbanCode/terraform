package org.urbancode.terraform.tasks.util;

import org.junit.Assert;
import org.junit.Test;
import org.urbancode.terraform.tasks.vmware.util.Ip4;
import org.urbancode.terraform.tasks.vmware.util.IpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.IpInUseException;

public class IpAddressPoolTest {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final Ip4 ONE = new Ip4("10.15.50.1");
    private static final Ip4 TWO = new Ip4("10.15.50.2");
    private static final Ip4 SIX = new Ip4("10.15.50.6");
    private static final Ip4 TEN = new Ip4("10.15.50.10");

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Test
    public void testIpNext() throws IpInUseException {
        IpAddressPool pool = new IpAddressPool("10.15.50.1", "10.15.50.250");
        //allocate first 5 IPs
        System.out.println("allocated " + pool.allocateIp().toString());
        System.out.println("allocated " + pool.allocateIp().toString());
        System.out.println("allocated " + pool.allocateIp().toString());
        System.out.println("allocated " + pool.allocateIp().toString());
        System.out.println("allocated " + pool.allocateIp().toString());
        Assert.assertEquals(true, pool.getNext().equals(SIX));
        pool.releaseIp(ONE);
        Assert.assertEquals(true, pool.getNext().equals(ONE));
        pool.releaseIp(TWO);
        Assert.assertEquals(true, pool.getNext().equals(ONE));
        pool.reserveIp(ONE);
        Assert.assertEquals(true, pool.getNext().equals(TWO));
        pool.reserveIp(TWO);
        Assert.assertEquals(true, pool.getNext().equals(SIX));
        pool.reserveIp(TEN);
        Assert.assertEquals(true, pool.getNext().equals(SIX));

    }
}

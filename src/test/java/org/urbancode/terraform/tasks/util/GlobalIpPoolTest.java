package org.urbancode.terraform.tasks.util;

import org.junit.Test;
import org.urbancode.terraform.tasks.vmware.util.GlobalIpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.IpAddressPool;

public class GlobalIpPoolTest {
    
    
    @Test
    public void instantiateTest() {
        GlobalIpAddressPool gap = GlobalIpAddressPool.getInstance();
        IpAddressPool pool = gap.getIpAddressPool();
        
        System.out.println(pool.getFirst().toString());
        System.out.println(pool.getLast().toString());
    }
    
}

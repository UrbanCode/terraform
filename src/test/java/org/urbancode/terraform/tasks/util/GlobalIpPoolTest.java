package org.urbancode.terraform.tasks.util;

import java.net.URL;

import org.junit.Test;

import com.urbancode.terraform.tasks.vmware.util.GlobalIpAddressPool;
import com.urbancode.terraform.tasks.vmware.util.IpAddressPool;

public class GlobalIpPoolTest {


    @Test
    public void instantiateTest() {
        URL in = null;
        in = Thread.currentThread().getContextClassLoader().getResource("ippool.conf");
        if (in != null) {
            System.setProperty("TERRAFORM_HOME", in.getPath());
            GlobalIpAddressPool gap = GlobalIpAddressPool.getInstance();
            IpAddressPool pool = gap.getIpAddressPool();

            System.out.println(pool.getFirst().toString());
            System.out.println(pool.getLast().toString());
        }
    }

}

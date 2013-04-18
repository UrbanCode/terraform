package com.urbancode.terraform.tasks.vcloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.x2o.tasks.SubTask;

public class NetworkTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(NetworkTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<FirewallRuleTask> firewallRuleTasks = new ArrayList<FirewallRuleTask>();
    private List<NATRuleTask> natRuleTasks = new ArrayList<NATRuleTask>();
    private DHCPTask dhcpTask;
    private StaticRoutingTask staticRoutingTask;
    
    private String networkName;
    private String href;
    private String fenceMode;
    
    //----------------------------------------------------------------------------------------------
    public List<FirewallRuleTask> getFirewallRuleTasks() {
        return firewallRuleTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<NATRuleTask> getNatRuleTasks() {
        return natRuleTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public DHCPTask getDhcpTask() {
        return dhcpTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public StaticRoutingTask getStaticRoutingTask() {
        return staticRoutingTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getNetworkName() {
        return networkName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getHref() {
        return href;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getFenceMode() {
        return fenceMode;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setHref(String href) {
        this.href = href;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setFenceMode(String fenceMode) {
        this.fenceMode = fenceMode;
    }
    
    //----------------------------------------------------------------------------------------------
    public FirewallRuleTask createFirewallRule() {
        FirewallRuleTask firewallRuleTask = new FirewallRuleTask();
        firewallRuleTasks.add(firewallRuleTask);
        return firewallRuleTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public NATRuleTask createNatRuleTask() {
        NATRuleTask natRuleTask = new NATRuleTask();
        natRuleTasks.add(natRuleTask);
        return natRuleTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public DHCPTask createDHCPTask() {
        dhcpTask = new DHCPTask();
        return dhcpTask;
    }
    
    //----------------------------------------------------------------------------------------------
    public StaticRoutingTask createStaticRoutingTask() {
        staticRoutingTask = new StaticRoutingTask();
        return staticRoutingTask;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub

    }

}

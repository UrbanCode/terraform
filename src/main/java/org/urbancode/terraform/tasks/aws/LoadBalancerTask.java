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
package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Listener;

public class LoadBalancerTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(LoadBalancerTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonElasticLoadBalancing elbClient;
    private AWSHelper helper;
    private ContextAWS context;
    
    private String loadBalancerName;
    private String subnetName;
    private String appCookieName;
    private String DNSName;
    
    private HealthCheckTask healthCheck;
    
    private List<VpcSecurityGroupRefTask> secGroupRefs = new ArrayList<VpcSecurityGroupRefTask>();
    private List<ListenerTask> listeners = new ArrayList<ListenerTask>();
    
    //----------------------------------------------------------------------------------------------
    LoadBalancerTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    //----------------------------------------------------------------------------------------------
    public void setDnsName(String DNSName) {
        this.DNSName = DNSName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setAppCookieName(String appCookieName) {
        this.appCookieName = appCookieName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return loadBalancerName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSubnetName() {
        return subnetName;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<ListenerTask> getListeners() {
        return Collections.unmodifiableList(listeners);
    }
    
    //----------------------------------------------------------------------------------------------
    public List<VpcSecurityGroupRefTask> getSecRefs() {
        return Collections.unmodifiableList(secGroupRefs);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getDnsName() {
        return DNSName;
    }

    //----------------------------------------------------------------------------------------------
    public HealthCheckTask getHealthCheck() {
        return healthCheck;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getAppCookieName() {
        return appCookieName;
    }
    
    //----------------------------------------------------------------------------------------------
    public VpcSecurityGroupRefTask createSecurityGroupRef() {
        VpcSecurityGroupRefTask group = new VpcSecurityGroupRefTask(context);
        secGroupRefs.add(group);
        return group;
    }
    
    //----------------------------------------------------------------------------------------------
    public HealthCheckTask createHealthCheck() {
        healthCheck = new HealthCheckTask(context);
        return healthCheck;
    }
    
    //----------------------------------------------------------------------------------------------
    public ListenerTask createListener() {
        ListenerTask listener = new ListenerTask(context);
        listeners.add(listener);
        return listener;
    }
    
    //----------------------------------------------------------------------------------------------
    // TODO - this should accept a List<String>
    private List<String> resolveSubnetIds(String subnetName) throws Exception {
        List<String> result = new ArrayList<String>();
        
        String tmp = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSubnetForName(subnetName).getId();
        
        result.add(tmp);
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private List<String> resolveSecGroupIds(List<VpcSecurityGroupRefTask> secGroupNames) throws Exception {
        List<String> result = new ArrayList<String>();
        
        
        if (secGroupNames != null && !secGroupNames.isEmpty()) {
            for (VpcSecurityGroupRefTask ref : secGroupNames) {
                String tmp = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc()
                                .findSecurityGroupForName(ref.getSecurityGroupName()).getId();
                result.add(tmp);
            }
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws EnvironmentCreationException {
        log.debug("LoadBalancerAWS: create()");
        if (DNSName == null) {
            if (elbClient == null) {
                elbClient = context.getELBClient();
            }
            
            String stickyPolicyName = "StickyPolicy";   // scope of the policy name? Will this need to change?
            long defaultCookieExp = 60000;
            
            // get amazon ids 
            List<String> subnetIds = null;
            List<String> secGroupIds = null;
            try {
                 subnetIds = resolveSubnetIds(getSubnetName());
                 if (subnetIds != null) {
                     secGroupIds = resolveSecGroupIds(getSecRefs());
        
                    List<Listener> listeners = new ArrayList<Listener>();
                    if (getListeners() != null) {
                        for (ListenerTask task : getListeners()) {
                            Listener tmp = new Listener(task.getProtocol(), task.getLoadBalancerPort(), task.getInstancePort());
                            if (task.isSecure()) {
                                tmp.setSSLCertificateId(task.getCertId());  // TODO - test
                            }
                            listeners.add(tmp);
                        }
                    }
                    else {
                        log.warn("No listeners specified for LoadBalancer: " + loadBalancerName 
                               + "\nThis load balancer is not configured to balance any instances.");
                    }
                    
                    // launch the load balancer
                    DNSName = helper.launchLoadBalancer(getName(), subnetIds, secGroupIds, listeners, elbClient);
                    
                    // configure sticky sessions
                    helper.createStickyPolicy(loadBalancerName, stickyPolicyName, getAppCookieName(), defaultCookieExp, elbClient);
                    
                    // configure the HealthChecks on the instances for them to be registered properly
                    if (getHealthCheck() != null) {
                        String hcTarget = getHealthCheck().getProtocol() + ":" + getHealthCheck().getPort() + getHealthCheck().getPath();
                        int health = getHealthCheck().getHealthyCount();
                        int unhealth = getHealthCheck().getUnhealthyCount();
                        int interval = getHealthCheck().getInterval();
                        int timeout = getHealthCheck().getTimeout();
                        helper.setupHealthCheck(getName(), hcTarget, health, unhealth, interval, timeout, elbClient);
                    }
                    else {
                        log.warn("No HealthCheck specified for load balancer " + getName()
                                + "\nYou may not be able to reach the instances behind this load balancer.");
                    }
                 } 
                 else {
                     String msg = "Could not find subnet " + getSubnetName() + " for load balancer " + getName() + 
                                  "\nLoad Balancer " + getName() + " not created.";
                     log.error(msg);
                     throw new EnvironmentCreationException(msg);
                 }
            }
            catch (Exception e) {
                log.error("Could not create load balancer " + loadBalancerName + " completely");
                throw new EnvironmentCreationException("Could not start load balancer " + loadBalancerName, e);
            }
            finally {
                elbClient = null;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws EnvironmentDestructionException {
        if (elbClient == null) {
            elbClient = context.getELBClient();
        }
        try {
            helper = context.getAWSHelper();
            helper.deleteLoadBalancer(getName(), elbClient);
            // will auto-delete policies and listeners associated with loadBalancer
        }
        catch (Exception e) {
            log.error("Could not create load balancer " + loadBalancerName + " completely");
            throw new EnvironmentDestructionException("Could not destroy load balancer " + loadBalancerName, e);
        }
        finally {
            setDnsName(null);
            elbClient = null;
        }
    }

}

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.urbancode.x2o.tasks.SubTask;

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
    private List<String> subnetNamesList;
    private String appCookieName;
    private String DNSName;
    private String zones;
    private List<String> zonesList;

    private HealthCheckTask healthCheck;

    private String fullName;

    private List<SecurityGroupRefTask> secGroupRefs = new ArrayList<SecurityGroupRefTask>();
    private List<ListenerTask> listeners = new ArrayList<ListenerTask>();

    //----------------------------------------------------------------------------------------------
    public LoadBalancerTask(ContextAWS context) {
        this.context = context;
        helper = new AWSHelper();
    }

    //----------------------------------------------------------------------------------------------
    private List<String> parseStringToList(String string) {
        List<String> result = new ArrayList<String>();

        // split on whitespace
        String[] tmp = string.split("\\s+");

        result = Arrays.asList(tmp);

        log.debug("Creating list out of " + string);
        for (String s : result) {
            s.trim();
            log.debug(s);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setZones(String zones) {
        this.zones = zones;
        zonesList = parseStringToList(zones);
    }

    //----------------------------------------------------------------------------------------------
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    //----------------------------------------------------------------------------------------------
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
        subnetNamesList = parseStringToList(subnetName);
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
    public String getFullName() {
        return fullName;
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
    public List<SecurityGroupRefTask> getSecRefs() {
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
    public String getZones() {
        return zones;
    }

    //----------------------------------------------------------------------------------------------
    public boolean containsZone(String zone) {
        boolean result = false;
        if (zonesList != null && zonesList.contains(zone)) {
            result = true;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public boolean containsSubnet(String subnetName) {
        boolean result = false;
        if (subnetNamesList != null && subnetNamesList.contains(subnetName)) {
            result = true;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public VpcSecurityGroupRefTask createVpcSecurityGroupRef() {
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
    private List<String> resolveSubnetIds(String subnetName)
    throws Exception {
        List<String> result = new ArrayList<String>();
        VpcTask vpc = null;

        // make sure we have a context and an EnvironmentAWS
        if (context != null && context.getEnvironment() != null
            && context.getEnvironment() instanceof EnvironmentTaskAWS) {

            EnvironmentTaskAWS env = (EnvironmentTaskAWS) context.getEnvironment();
            // grab the vpc
            if (env.getVpc() != null) {
                vpc = env.getVpc();
            }
            else {
                throw new NullPointerException("No VPC found for load balancer "
                        + fullName);
            }
        }
        // prevent against duplicate Zones in subnets
        List<String> usedZones = new ArrayList<String>();

        if (subnetNamesList != null) {
            for (String name : subnetNamesList) {
                SubnetTask tmp = vpc.findSubnetForName(name);
                if (tmp != null && tmp.getId() != null) {
                    log.debug("Adding subnetId " + tmp.getId() + " to load balancer "
                              + fullName);
                    String curZone = tmp.getZone();
                    if (!usedZones.contains(curZone)) {
                        usedZones.add(curZone);
                        result.add(tmp.getId());
                    }
                    else {
                        log.warn("Not adding Subnet " + tmp.getName() + " to load balancer "
                                 + fullName + " because zone " + tmp.getZone() + " is "
                                 + "already used on the load balancer.");
                    }
                }
                else {
                    log.error("Could not find subnet " + name + " in the VPC");
                }
            }
        }
        else {
            log.error("No Subnets listed");
            throw new NullPointerException("Loadbalancer must have a list of Subnet names if " +
                                            "launching into VPC");
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private List<String> resolveSecGroupIds(List<SecurityGroupRefTask> list)
    throws Exception {
        List<String> result = new ArrayList<String>();
        if (list != null && !list.isEmpty()) {
            // Check out the env
            EnvironmentTaskAWS env = null;
            env = (EnvironmentTaskAWS) context.getEnvironment();

            for (SecurityGroupRefTask ref : list) {
                String id = null;
                if (ref instanceof VpcSecurityGroupRefTask) {
                    VpcSecurityGroupTask tmp =
                            env.getVpc().findSecurityGroupForName(ref.getSecurityGroupName());
                    if (tmp != null) {
                        id = tmp.getId();
                    }
                    else {
                        log.error("Security Group " + ref.getSecurityGroupName() +
                                  " not found in VPC in environment " + env.getName());
                    }
                }
                result.add(id);
            }
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private List<String> resolveZones(String zones) {

        // prevent duplicate zones
        List<String> usedZones = new ArrayList<String>();
        if (zonesList != null) {
            for (String zone : zonesList) {
                if (!usedZones.contains(zone)) {
                    usedZones.add(zone);

                }
                else {
                    log.warn("");
                }
            }
        }

        return usedZones;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create()
    throws EnvironmentCreationException {
        if (DNSName == null) {
            if (elbClient == null) {
                elbClient = context.fetchELBClient();
            }

            String uuid = context.getEnvironment().fetchSuffix();
            fullName = loadBalancerName + ("-" + uuid);
            log.debug("Security Group " + loadBalancerName + " has fullname " + fullName);

            String stickyPolicyName = "StickyPolicy";
            long defaultCookieExp = 60000;

            // get amazon ids
            List<String> subnetIds = null;
            List<String> availZones = null;
            List<String> secGroupIds = null;
            try {

                if (subnetName != null && !subnetName.isEmpty()) {
                    subnetIds = resolveSubnetIds(subnetName);
                }
                else {
                    log.warn("No subnets specified on load balancer " + fullName);
                    if (zones != null && !zones.isEmpty()) {
                        availZones = resolveZones(zones);
                    }
                    else {
                        log.warn("No zones specified on load balancer " + fullName);
                        throw new EnvironmentCreationException("Must specify either zones or "
                                + "subnets on load balancer " + fullName);
                    }
                }

                secGroupIds = resolveSecGroupIds(getSecRefs());

                List<Listener> listeners = new ArrayList<Listener>();
                if (getListeners() != null) {
                    for (ListenerTask task : getListeners()) {
                        Listener tmp = new Listener(task.getProtocol(),
                                task.getLoadBalancerPort(), task.getInstancePort());
                        if (task.isSecure()) {
                            tmp.setSSLCertificateId(task.getCertId());
                        }
                        listeners.add(tmp);
                    }
                }
                else {
                    log.warn("No listeners specified for LoadBalancer: " + fullName
                           + "\nThis load balancer is not configured to balance any "
                           + "instances.");
                }

                // launch the load balancer
                DNSName = helper.launchLoadBalancer(fullName, subnetIds, secGroupIds,
                        listeners, availZones, elbClient);

                // configure sticky sessions
                helper.createStickyPolicy(fullName, stickyPolicyName,
                        getAppCookieName(), defaultCookieExp, elbClient);

                // configure the HealthChecks on the instances for them to be registered
                if (getHealthCheck() != null) {
                    String hcTarget = getHealthCheck().getProtocol() + ":" + getHealthCheck()
                                        .getPort() + getHealthCheck().getPath();
                    int health = getHealthCheck().getHealthyCount();
                    int unhealth = getHealthCheck().getUnhealthyCount();
                    int interval = getHealthCheck().getInterval();
                    int timeout = getHealthCheck().getTimeout();
                    helper.setupHealthCheck(fullName, hcTarget, health, unhealth, interval,
                                            timeout, elbClient);
                }
                else {
                    log.warn("No HealthCheck specified for load balancer " + fullName
                            + "\nYou may not be able to reach the instances behind this "
                            + "load balancer.");
                }
            }
            catch (Exception e) {
                log.error("Could not create load balancer " + fullName + " completely");
                throw new EnvironmentCreationException("Could not start load balancer " +
                        fullName, e);
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
            elbClient = context.fetchELBClient();
        }
        try {
            helper = context.getAWSHelper();
            helper.deleteLoadBalancer(fullName, elbClient);
            // will auto-delete policies and listeners associated with loadBalancer
        }
        catch (Exception e) {
            log.error("Could not create load balancer " + fullName + " completely");
            throw new EnvironmentDestructionException("Could not destroy load balancer " +
                    fullName, e);
        }
        finally {
            setDnsName(null);
            elbClient = null;
        }
    }

}

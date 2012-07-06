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
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Subnet;

public class SubnetTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(SubnetTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;
    
    private String vpcId;
    private String zone;
    private String cidr;
    private String name;
    private String subnetId;
    private RouteTableTask routeTable;
    
    //----------------------------------------------------------------------------------------------
    SubnetTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return subnetId;
    }

    //----------------------------------------------------------------------------------------------
    public String getZone() {
        return zone;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getCidr() {
        return cidr;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public RouteTableTask getRouteTable() {
        return routeTable;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setRouteTable(RouteTableTask routeTable) {
        this.routeTable = routeTable;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCidr(String cidr) {
        this.cidr = cidr;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setZone(String zone) {
        this.zone = zone;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.subnetId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean existsInAws() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        boolean result = false;
        List<String> id = new ArrayList<String>();
        id.add(subnetId);
        
        List<Subnet> subnets = helper.getSubnets(id, ec2Client);
        
        if (subnets != null && !subnets.isEmpty()) {
            result = true;
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean verify() {
        // will return false if the id is null
        boolean result = false;
        if (subnetId != null) {
            if (ec2Client == null) {
                ec2Client = context.getEC2Client();
            }
            
            List<String> id = new ArrayList<String>();
            id.add(subnetId);
            
            List<Subnet> subnets = helper.getSubnets(id, ec2Client);
            if (subnets != null && !subnets.isEmpty()) {
                for (Subnet subnet : subnets) {
                    if (subnet.getAvailabilityZone().equalsIgnoreCase(zone)) {
                        if (subnet.getCidrBlock().equals(cidr)) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws EnvironmentCreationException {
        boolean verified = false;
        
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }

        try {
            if (subnetId != null) {
                verified = verify();
            }
            
            if (!verified) {
                setId(null);
                log.info("Creating Subnet...");
                setId(helper.createSubnet(vpcId, cidr, zone, ec2Client));
                log.info("Subnet " + name + " created with id: " + subnetId);
                helper.tagInstance(subnetId, "terraform.environment", context.getEnvironment().getName(), ec2Client);
            }
            else {
                log.info("Subnet " + name + " : " + subnetId + " already exists in AWS.");
            }
        }
        catch (Exception e) {
            throw new EnvironmentCreationException("Could not create Subnet completely", e);
        }
        finally {
            ec2Client = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws EnvironmentDestructionException {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            log.info("Destroying Subnet...");
            helper.deleteSubnet(subnetId, ec2Client);
            log.info("Subnet " + name + " : " + subnetId + " destroyed.");
            setId(null);
        }
        catch (Exception e) {
            throw new EnvironmentDestructionException("Could not destroy Subnet completely", e);
        }
        finally {
            ec2Client = null;
        }
    }
}

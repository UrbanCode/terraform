package com.urbancode.uprovision.tasks.aws;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Subnet;
import com.urbancode.uprovision.tasks.aws.helpers.AWSHelper;
import com.urbancode.uprovision.tasks.common.SubTask;

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
        
        List<Subnet> subnets = helper.describeSubnets(id, ec2Client);
        
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
            
            List<Subnet> subnets = helper.describeSubnets(id, ec2Client);
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
    throws Exception {
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
            }
            else {
                log.info("Subnet " + name + " : " + subnetId + " already exists in AWS.");
            }
        }
        finally {
            ec2Client = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws Exception {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            log.info("Destroying Subnet...");
            helper.deleteSubnet(subnetId, ec2Client);
            log.info("Subnet " + name + " : " + subnetId + " destroyed.");
            setId(null);
        }
        finally {
            ec2Client = null;
        }
    }
}

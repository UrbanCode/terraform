package com.urbancode.uprovision.tasks.aws;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateRouteRequest;
import com.urbancode.uprovision.tasks.aws.helpers.AWSHelper;
import com.urbancode.uprovision.tasks.common.SubTask;

public class RouteTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(RouteTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;

    private String routeId;
    private String routeTableId;
    private String target;
    private String cidr;
    private String targetName;

    //----------------------------------------------------------------------------------------------
    RouteTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return routeId;
    }

    //----------------------------------------------------------------------------------------------
    public String getRouteTableId() {
        return routeTableId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getTarget() {
        return target;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getTargetName() {
        return targetName;
    }
   
    //----------------------------------------------------------------------------------------------
    public String getDest() {
        return cidr;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.routeId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setRouteTableId(String id) {
        this.routeTableId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setTarget(String id) {
        this.target = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setDest(String cidr) {
        this.cidr = cidr;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        log.info("Creating Route");
        try {
            if (target == null || cidr == null || routeTableId == null) {
                log.error("Error: Route has bad data.");
                return;
            }
            helper.createRoute(routeTableId, cidr, target, ec2Client);
            log.info("Route Created.");
        }
        finally {
            ec2Client = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            helper.deleteRoute(routeTableId, cidr, ec2Client);
        }
        finally {
            ec2Client = null;
        }
    }
}

package com.urbancode.uprovision.tasks.aws;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.InternetGatewayAttachment;
import com.urbancode.uprovision.tasks.aws.helpers.AWSHelper;
import com.urbancode.uprovision.tasks.common.SubTask;

public class InetGwyTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(InetGwyTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;

    private String gatewayId;
    private String vpcId;
    private String name;
    
    //----------------------------------------------------------------------------------------------
    InetGwyTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return gatewayId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.gatewayId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean existsInAws() {
        boolean result = false; 
        DescribeInternetGatewaysRequest req = new DescribeInternetGatewaysRequest().withInternetGatewayIds(getId());
        if (context.getEC2Client().describeInternetGateways(req).getInternetGateways().isEmpty()) {
            log.error("InternetGateway ( " + getId() + " ) does not exist in AWS!");
        }
        else {
            result = true;
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean verify() {
        // will return false if the id is null
        boolean result = false;
        if (gatewayId != null) {
            if (ec2Client == null) {
                ec2Client = context.getEC2Client();
            }
            
            List<String> id = new ArrayList<String>();
            id.add(gatewayId);
            
            List<InternetGateway> gateways = helper.describeInternetGateways(id, ec2Client);
            if (gateways != null && !gateways.isEmpty()) {
                for (InternetGateway gateway : gateways) {
                    List<InternetGatewayAttachment> attachments = gateway.getAttachments();
                    if (attachments != null && !attachments.isEmpty()) {
                        for (InternetGatewayAttachment attachment : attachments) {
                            String attachedVpc = attachment.getVpcId();
                            if (vpcId.equals(attachedVpc)) {
                                result = true;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        boolean verified = false;
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            if (gatewayId != null) {
                verified = verify();
            }
            
            if(!verified)  {
                setId(null);
                log.info("Creating InternetGateway with AWS connection : " + ec2Client);
                setId(helper.createInternetGateway(ec2Client));
                log.info("InternetGateway created with gatewayId: " + getId());
                helper.attachInternetGatewayToVpc(gatewayId, vpcId, ec2Client);
            }
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
            log.info("Destroying InternetGateway");
            if (vpcId != null) {
                helper.detachGateway(gatewayId, vpcId, ec2Client);
            }
            helper.deleteInternetGateway(gatewayId, ec2Client);
        }
        finally {
            setId(null);
            ec2Client = null;
            log.info("InternetGateway Destroyed.");
        }
        
    }
}

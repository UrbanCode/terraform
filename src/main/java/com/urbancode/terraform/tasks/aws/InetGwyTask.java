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
package com.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.InternetGatewayAttachment;
import com.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import com.urbancode.x2o.tasks.SubTask;

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
    public InetGwyTask(ContextAWS context) {
        this.context = context;
        helper = new AWSHelper();
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
        if (context.fetchEC2Client().describeInternetGateways(req).getInternetGateways().isEmpty()) {
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
                ec2Client = context.fetchEC2Client();
            }

            List<String> id = new ArrayList<String>();
            id.add(gatewayId);

            List<InternetGateway> gateways = helper.getInternetGateways(id, ec2Client);
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
            ec2Client = context.fetchEC2Client();
        }

        try {
            if (gatewayId != null) {
                verified = verify();
            }

            if(!verified)  {
                setId(null);
                log.info("Creating InternetGateway with AWS connection : " + ec2Client);
                setId(helper.createInternetGateway(ec2Client));
                helper.tagInstance(gatewayId, "terraform.environment", context.getEnvironment().getName(), ec2Client);
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
            ec2Client = context.fetchEC2Client();
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

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

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.ec2.AmazonEC2;

public class RuleTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(RuleTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;

    private boolean inbound;
    private int start;
    private int end;
    private String protocol;
    private String source;
    private String groupId;


    //----------------------------------------------------------------------------------------------
    public RuleTask(ContextAWS context) {
        this.context = context;
        helper = new AWSHelper();
    }

    //----------------------------------------------------------------------------------------------
    public int getStartPort() {
        return start;
    }

    //----------------------------------------------------------------------------------------------
    public int getEndPort() {
        return end;
    }

    //----------------------------------------------------------------------------------------------
    public String getSource() {
        return source;
    }

    //----------------------------------------------------------------------------------------------
    public String getProtocol() {
        return protocol;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getInbound() {
        return inbound;
    }

    //----------------------------------------------------------------------------------------------
    public void setGroupId(String id) {
        this.groupId = id;
    }

    //----------------------------------------------------------------------------------------------
    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    //----------------------------------------------------------------------------------------------
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    //----------------------------------------------------------------------------------------------
    public void setStartPort(int start) {
        this.start = start;
    }

    //----------------------------------------------------------------------------------------------
    public void setEndPort(int end) {
        this.end = end;
    }

    //----------------------------------------------------------------------------------------------
    public void setSource(String source) {
        this.source = source;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        if (ec2Client == null) {
            ec2Client = context.fetchEC2Client();
        }

        log.info("Creating Rule...");

        try {
            String cidr = "0.0.0.0/0";
            helper.createRuleForSecurityGroup(groupId, protocol, start, end, cidr, inbound, ec2Client);
            log.info("Rule Created.");
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
            helper.deleteRuleForSecurityGroup(groupId, protocol, start , end, source, inbound, ec2Client);
        }
        finally {
            ec2Client = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {
        // TODO Auto-generated method stub

    }
}

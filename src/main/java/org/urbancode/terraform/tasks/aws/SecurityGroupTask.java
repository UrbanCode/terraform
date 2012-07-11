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
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.SecurityGroup;


public abstract class SecurityGroupTask extends SubTask {



    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(SecurityGroupTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    protected AmazonEC2 ec2Client;
    protected AWSHelper helper;
    protected ContextAWS context;
    
    protected String vpcId = null;   // not used in EC2, but it makes things a lot easier to keep this here
    
    protected String name;
    protected String descr;
    protected String groupId;
    protected List<RuleTask> rules;
    
    //----------------------------------------------------------------------------------------------
    SecurityGroupTask(Context context) {
        super(context);
        this.context = (ContextAWS) context;
        helper = new AWSHelper();
        rules = new ArrayList<RuleTask>();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.groupId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setDescription(String descr) {
        this.descr = descr;
    }

    //----------------------------------------------------------------------------------------------
    public String getId() {
        return groupId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getDescription() {
        return descr;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<RuleTask> getRules() {
        return Collections.unmodifiableList(rules);
    }
    
    //----------------------------------------------------------------------------------------------
    public RuleTask createRule() {
        RuleTask rule = new RuleTask(context);
        rules.add(rule);
        return rule;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean existsInAws() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        boolean result = false;
        List<String> id = new ArrayList<String>();
        id.add(groupId);
        
        List<SecurityGroup> group = helper.getSecurityGroups(id, ec2Client);
        
        if (group != null && !group.isEmpty()) {
            result = true;
        }
        
        return result;
    }
     
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws EnvironmentCreationException {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            log.info("Creating SecurityGroup");
            setId(helper.createSecurityGroup(name, vpcId, descr, ec2Client));
            log.info("SecurityGroup " + name + " created with id: " + groupId);
            helper.tagInstance(groupId, "terraform.environment", context.getEnvironment().getName(), ec2Client);
            
            for (RuleTask rule : getRules()) {
                rule.setGroupId(groupId);
                rule.create();
            }
        }
        catch (Exception e) {
            throw new EnvironmentCreationException("Could not create Security Group completely.", e);
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
            log.info("Destroying SecurityGroup...");
            helper.deleteSecurityGroup(groupId, ec2Client);
            log.info("SecurityGroup " + name + " : " + groupId + " destroyed");
            setId(null);
        }
        catch (Exception e) {
            throw new EnvironmentDestructionException("Could not destroy Security Group completely.", e);
        }
        finally {
            ec2Client = null;
        }
    }
}

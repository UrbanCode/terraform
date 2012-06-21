package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;

public class VpcSecurityGroupTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(VpcSecurityGroupTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;
    
    private String name;
    private String descr;
    private String groupId;
    private String vpcId;
    private List<RuleTask> rules;
    
    //----------------------------------------------------------------------------------------------
    VpcSecurityGroupTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
        rules = new ArrayList<RuleTask>();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
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
        
        List<SecurityGroup> group = helper.describeSecurityGroups(id, ec2Client);
        
        if (group != null && !group.isEmpty()) {
            result = true;
        }
        
        return result;
    }
     
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            log.info("Creating SecurityGroup");
            setId(helper.createSecurityGroup(name, vpcId, descr, ec2Client));
            log.info("SecurityGroup " + name + " created with id: " + groupId);
            
            for (RuleTask rule : getRules()) {
                rule.setGroupId(groupId);
                rule.create();
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
            log.info("Destroying SecurityGroup...");
            helper.deleteSecurityGroup(groupId, ec2Client);
            log.info("SecurityGroup " + name + " : " + groupId + " destroyed");
            setId(null);
        }
        finally {
            ec2Client = null;
        }
    }
}

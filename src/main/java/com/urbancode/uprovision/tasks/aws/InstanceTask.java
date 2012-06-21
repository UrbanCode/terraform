package com.urbancode.uprovision.tasks.aws;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.urbancode.uprovision.tasks.EnvironmentCreationException;
import com.urbancode.uprovision.tasks.EnvironmentDestructionException;
import com.urbancode.uprovision.tasks.aws.helpers.AWSHelper;
import com.urbancode.uprovision.tasks.common.Task;

public class InstanceTask extends Task {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(InstanceTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AmazonElasticLoadBalancing elbClient;
    private AWSHelper helper;
    protected ContextAWS context;
    
    private boolean elasticIp;
    
    private String name;
    private String instanceId;
    private String amiId;
    private String subnetName;
    private String subnetId;
    private String elasticIpAllocId;
    private String elasticIpAddress;
    private String keyRef;
    private String sizeType;
    private String userData;
    private String loadBalancer;
    private String privateIp;
    private int count;
    private int priority;
    
    private BootActionsTask bootActions;
    private PostCreateActionsTask pca;
    private List<SecurityGroupRefTask> secRefs = new ArrayList<SecurityGroupRefTask>();
    private List<EbsTask> ebsVolumes = new ArrayList<EbsTask>();
    
    //----------------------------------------------------------------------------------------------
    public InstanceTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCount(int count) {
        this.count = count;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPrivateKeyRef(String keyRef) {
        this.keyRef = keyRef;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setImageSize(String sizeType) {
        this.sizeType = sizeType;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.instanceId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setAmiId(String id) {
        this.amiId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setSubnetName(String name) {
        this.subnetName = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setSubnetId(String id) {
        this.subnetId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setElasticIpAddress(String elasticIpAddress) {
        this.elasticIpAddress = elasticIpAddress;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setElasticIp(boolean elasticIp) {
        this.elasticIp = elasticIp;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setElasticIpAllocId(String id) {
        this.elasticIpAllocId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getPrivateIp() {
        return privateIp;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getLoadBalancer() {
        return loadBalancer;
    }
    
    //----------------------------------------------------------------------------------------------
    public int getPriority() {
        return priority;                  
    }
    
    //----------------------------------------------------------------------------------------------
    public int getCount() {
        return count;                  
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return instanceId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getImageSize() {
        return sizeType;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getPrivateKeyRef() {
        return keyRef;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public BootActionsTask getBootActions() {
        return bootActions;
    }
    
    //----------------------------------------------------------------------------------------------
    public PostCreateActionsTask getPostCreateActions() {
        return pca;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<SecurityGroupRefTask> getSecurityGroupRefs() {
        return Collections.unmodifiableList(secRefs);
    }
    
    //----------------------------------------------------------------------------------------------
    public List<EbsTask> getEbsVolumes() {
        return Collections.unmodifiableList(ebsVolumes);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getAmiId() {
        return amiId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSubnetId() {
        return subnetId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSubnetName() {
        return subnetName;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean getElasticIp() {
        return elasticIp;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getElasticIpAllocId() {
        return elasticIpAllocId;
    }

    //----------------------------------------------------------------------------------------------
    public String getElasticIpAddress() {
        return elasticIpAddress;
    }
    
    
    //----------------------------------------------------------------------------------------------
    public EbsTask createEbs() {
        EbsTask ebs = new EbsTask(context);
        ebsVolumes.add(ebs);
        return ebs;
    }
    
    //----------------------------------------------------------------------------------------------
    public BootActionsTask createBootActions() {
        this.bootActions = new BootActionsTask(context);
        return bootActions;
    }
    
    //----------------------------------------------------------------------------------------------
    public PostCreateActionsTask createPostCreateActions() {
        this.pca = new PostCreateActionsTask(context);
        return pca;
    }
    
    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask createSecurityGroupRef() {
        SecurityGroupRefTask sec = new SecurityGroupRefTask(context);
        secRefs.add(sec);
        return sec;
    }
    
    private boolean verifyElasticIp(Instance instance) {
        boolean result = false;
        boolean hasEIP = instance.getPublicIpAddress() != null;
        if (elasticIp == hasEIP) {
            if (elasticIp == true) {
                if (instance.getPublicIpAddress().equals(elasticIpAddress)) {
                    result = true;
                }
            }
            else {
                result = true;
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private boolean verifyKeyPair(Instance instance) {
        boolean result = false;
        String keyName = context.getKeyByName(keyRef);
        if (instance.getKeyName() != null && keyName != null) {
            if (instance.getKeyName().equals(keyName)) {
                result = true;
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private boolean verifySize(Instance instance) {
        boolean result = false;
        String size = context.getSizeByName(sizeType);
        if (instance.getInstanceType() != null && size != null) {
            if (instance.getInstanceType().equals(size)) {
                result = true;
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private boolean verifySecurityGroups(Instance instance) throws Exception {
        boolean result = false;
        List<String> expectedIds = new ArrayList<String>();
        for (SecurityGroupRefTask group : getSecurityGroupRefs()) {
            expectedIds.add(group.fetchSecurityGroup().getId());
        }
        List<String> foundIds = new ArrayList<String>();
        List<GroupIdentifier> gids = instance.getSecurityGroups();
        if (gids != null && !gids.isEmpty()) {
            for (GroupIdentifier gid : gids) {
                foundIds.add(gid.getGroupId());
            }
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean verify() throws Exception {
        // will return false if the id is null
        boolean result = false;
        if (instanceId != null) {
            if (ec2Client == null) {
                ec2Client = context.getEC2Client();
            }
            
            List<String> id = new ArrayList<String>();
            id.add(instanceId);
            
            List<Instance> instances = helper.describeInstances(id, ec2Client);
            if (instances != null && !instances.isEmpty()) {
                for (Instance instance : instances) {
                    if (instance.getImageId().equals(amiId)) {
                        String subId = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSubnetForName(subnetName).getId();
                        if (instance.getSubnetId() != null && instance.getSubnetId().equals(subId)) {
                            if (verifyElasticIp(instance)) {
                                if (verifyKeyPair(instance)) {
                                    if (verifySize(instance)) {
                                        if (verifySecurityGroups(instance)) {
                                            result = true;
                                        }
                                    }
                                }
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
    public void create() 
    throws Exception {
        log.debug("InstanceAWS: create()");
        boolean verified = false;
        context.setProperty("server.name", name);  // update server.name prop
        
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        if (elbClient == null) {
            elbClient = context.getELBClient();
        }
        
        try {
            if (instanceId != null) {
                verified = verify();
            }
            
            if (getBootActions() != null) {
                getBootActions().create();
                userData = getBootActions().getUserData();
                userData = context.resolve(userData);
            }
            
            log.info("Instance is being launched with following user-data script:\n\n" + userData);
            
            String keyPair = keyRef;
            String size = context.getSizeByName(sizeType.toLowerCase());
            
            if (!verified) {
                setId(null);
                log.info("Creating Instance...");
                
                // add security groups
                List<String> groupIds = new ArrayList<String>();
                for (SecurityGroupRefTask ref : getSecurityGroupRefs()) {
                    groupIds.add(ref.fetchSecurityGroup().getId());
                }
                
                // do Ebs
                List<BlockDeviceMapping> blockMaps = new ArrayList<BlockDeviceMapping>();
                if (ebsVolumes != null) {
                    for (EbsTask ebs : ebsVolumes) {
                        ebs.create();
                        blockMaps.add(ebs.getBlockDeviceMapping());
                    }
                }
                
                // set the instanceId
                instanceId = helper.launchAmi(amiId, subnetId, keyPair, size, userData, groupIds, blockMaps, ec2Client);
                
                
                // wait for instance to start and pass status checks
                helper.waitForState(instanceId, "running", 8, ec2Client);
                helper.waitForStatus(instanceId, "ok", 8, ec2Client);
                
                // name Instances
                String serverName = context.getEnvironment().getName() + "-" + name;
                helper.tagInstance(instanceId, "Name", serverName, ec2Client);
                
                // give instance elastic ip
                if (elasticIp) {
                    setElasticIpAllocId(helper.requestElasticIp(ec2Client));
                    setElasticIpAddress(helper.assignElasticIp(instanceId, elasticIpAllocId, ec2Client));
                    context.setProperty(getName() + ".public.ip", getElasticIpAddress());
                }
                
                // set private ip
                privateIp = helper.getPrivateIp(instanceId, ec2Client);
                context.setProperty(getName() + ".private.ip", getPrivateIp());
                
                // register with LB
                List<String> tmp = new ArrayList<String>();
                tmp.add(instanceId);
                if (loadBalancer != null && !"".equals(loadBalancer)) {
                    helper.updateInstancesOnLoadBalancer(loadBalancer, tmp, true, elbClient);
                }
                
                // do PostCreateActions
                if (pca != null) {
                    if (elasticIpAddress != null && !elasticIpAddress.isEmpty()) {
                        pca.setHost(elasticIpAddress);
                    }
                    else {
                        log.warn("Trying to do PostCreateActions on instance with no public ip!" 
                                + "\nName: " + name 
                                + "\nId: " + instanceId);
                    }
                    
                    if (keyPair != null && !keyPair.isEmpty()) {
                        String basePath = "/home/ncc/.ec2";
                        String keyPairPath = basePath + File.separator + keyPair + ".pem";
                        pca.setIdFile(keyPairPath);
                    }
                    else {
                        log.warn("Trying to do PostCreateActions on instance with no ssh key!" 
                                + "\nName: " + name 
                                + "\nId: " + instanceId);
                    }
                    
                    pca.create();
                }
            }
            
        }
        catch (Exception e) {
            log.error("Did not start instance " + name + " completely");
            throw new EnvironmentCreationException("Failed to create Instance " + name, e);
        }
        finally {
            ec2Client = null;
            elbClient = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws Exception {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        if (elbClient == null) {
            elbClient = context.getELBClient();
        }
         
        try {
            log.info("Shutting down instance " + getId());
            
            List<String> instanceIds = new ArrayList<String>();
            instanceIds.add(instanceId);
            
            if (loadBalancer != null && !"".equals(loadBalancer)) {
                helper.updateInstancesOnLoadBalancer(loadBalancer, instanceIds, false, elbClient);
            }
            
            if (elasticIpAllocId != null) {
                String assocId = helper.getAssociationIdForAllocationId(elasticIpAllocId, ec2Client);
                helper.disassociateElasticIp(assocId, ec2Client);
                helper.releaseElasticIp(getElasticIpAllocId(), ec2Client);
            }
            
            helper.terminateInstances(instanceIds, ec2Client);
            helper.waitForState(getId(), "terminated", 8, ec2Client);
            
            for (SecurityGroupRefTask group : secRefs) {
                group.destroy();
            }
            // clear all attributes
            setId(null);
            setSubnetId(null);
            setElasticIpAllocId(null);
            setElasticIpAddress(null);
        }
        catch (Exception e) {
            log.error("Did not destroy instance " + name + " completely");
            throw new EnvironmentDestructionException("Failed to destroy instance " + name, e);
        }
        finally {
            ec2Client = null;
            elbClient = null;
            log.info("Instance Destroyed.");
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public InstanceTask clone() {
        InstanceTask result = new InstanceTask(context);
        
        // this takes up more memory than needed???
        result.setAmiId(amiId);
        result.setElasticIp(elasticIp);
        result.setImageSize(sizeType);
        result.setName(name);
        result.setPrivateKeyRef(keyRef);
        result.setSubnetName(subnetName);
        
        // post create actions task
        
        BootActionsTask pcat = result.createBootActions();
        if (getPostCreateActions() != null) {
            
            if (getBootActions().getShell() != null) {
                pcat.setShell(getBootActions().getShell());
            }
            
            if (getBootActions().getScript() != null) {
                for (PostCreateSubTask script : getBootActions().getScript()) {
                    ScriptTask scriptTask = (ScriptTask) script;
                    ScriptTask nScript = pcat.createScript();
                    nScript.setCmds(script.getCmds());
                    nScript.setShell(scriptTask.getShell());
                    nScript.setUrl(scriptTask.getUrl());
                    for (ParamTask param : scriptTask.getParams()) {
                        ParamTask nParam = nScript.createParam();
                        nParam.setValue(param.getValue());
                    }
                }
            }
        }
        
        // sec group refs
        if (getSecurityGroupRefs() != null) {
            for (SecurityGroupRefTask secGroup : getSecurityGroupRefs()) {
                SecurityGroupRefTask nSecGroup = result.createSecurityGroupRef();
                nSecGroup.setSecurityGroupName(secGroup.getSecurityGroupName());
            }
        }
        
        // ebsVolumes
        if (getEbsVolumes() != null) {
            for (EbsTask ebs : getEbsVolumes()) {
                EbsTask nEbs = result.createEbs();
                nEbs.setVolumeSize(ebs.getVolumeSize());
                nEbs.setVolumeId(ebs.getVolumeId());
                nEbs.setSnapshotId(ebs.getSnapshotId());
                nEbs.setPersist(ebs.getPersist());
                nEbs.setName(ebs.getName());
                nEbs.setDeviceName(ebs.getDeviceName());
            }
       }
        
        return result;
    }
}

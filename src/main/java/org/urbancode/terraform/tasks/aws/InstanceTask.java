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

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.common.exceptions.PostCreateException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;

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

    // by default, do not assign an EIP
    private boolean elasticIp = false;

    private String name;
    private String instanceId;
    private String amiId;
    private String akiId;
    private String ariId;
    private String subnetName;
    private String subnetId;
    private String elasticIpAllocId;
    private String elasticIpAddress;
    private String keyRef;
    private String sizeType;
    private String userData;
    private String loadBalancer;
    private String privateIp;
    private String zone;

    private String platform;

    // default values
    private int count = 1;
    private int priority = 1;

    private BootActionsTask bootActions;
    private PostCreateActionsTask pca;
    private List<SecurityGroupRefTask> secRefs = new ArrayList<SecurityGroupRefTask>();
    private List<EbsTask> ebsVolumes = new ArrayList<EbsTask>();

    //----------------------------------------------------------------------------------------------
    public InstanceTask(ContextAWS context) {
        this.context = context;
        helper = new AWSHelper();
    }

    //----------------------------------------------------------------------------------------------
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    //----------------------------------------------------------------------------------------------
    public void setZone(String zone) {
        this.zone = zone;
    }

    //----------------------------------------------------------------------------------------------
    public void setPrivateIp(String privateIp) {

        String ip4RegEx = "[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?";
        Pattern ip4Pattern = Pattern.compile(ip4RegEx);
        Matcher ip4Matcher = ip4Pattern.matcher(privateIp);
        if (!(ip4Matcher.find() && ip4Matcher.end() == privateIp.length())) {
            throw new EnvironmentCreationException("Bad Private IP Address Format");
        }
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
    public void setPublicIp(String elasticIpAddress) {
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
    public void setRamdiskId(String ariId) {
        this.ariId = ariId;
    }
    //----------------------------------------------------------------------------------------------
    public void setKernelId(String akiId) {
        this.akiId = akiId;
    }

    //----------------------------------------------------------------------------------------------
    public String getPlatform() {
        return platform;
    }

    //----------------------------------------------------------------------------------------------
    public String getZone() {
        return zone;
    }

    //----------------------------------------------------------------------------------------------
    public String getRamdiskId() {
        return ariId;
    }

    //----------------------------------------------------------------------------------------------
    public String getKernelId() {
        return akiId;
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
    public String getPublicIp() {
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
    public SecurityGroupRefTask createEc2SecurityGroupRef() {
        SecurityGroupRefTask group = null;

        // if the instance does not have a subnet name set, then we'll
        // check for a zone, which would be required for an EC2 instance
        if (zone != null && !zone.isEmpty()) {
            log.debug("Creating EC2 Security Group Ref");
            group = new Ec2SecurityGroupRefTask(context);
        }
        else {
            String msg = "No zone set! Unable to create securityGroup";
            log.error(msg);
            // throw
        }

        if (group != null) {
            secRefs.add(group);
        }

        return group;
    }

    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask createVpcSecurityGroupRef() {
        SecurityGroupRefTask group = null;

        // determine which type of security group we need to make.
        // We will check if it's a VPC one first, that means the
        // instance must have a subnet name set.
        if (subnetName != null && !subnetName.isEmpty()) {
            log.debug("Creating VPC Security Group Ref");
            group = new VpcSecurityGroupRefTask(context);
        }
        // if neither of those checks pass, something is wrong...
        else {
            String msg = "No subnet set! Unable to create securityGroup";
            log.error(msg);
            // throw
        }

        if (group != null) {
            secRefs.add(group);
        }

        return group;
    }

    //----------------------------------------------------------------------------------------------
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
        String keyName = keyRef;
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
        String size = sizeType;
        if (instance.getInstanceType() != null && size != null) {
            if (instance.getInstanceType().equals(size)) {
                result = true;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private boolean verifySecurityGroups(Instance instance) {
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
    public boolean verify() {
        // will return false if the id is null
        boolean result = false;
        if (instanceId != null) {
            if (ec2Client == null) {
                ec2Client = context.fetchEC2Client();
            }

            List<String> id = new ArrayList<String>();
            id.add(instanceId);

            List<Instance> instances = helper.getInstances(id, ec2Client);
            if (instances != null && !instances.isEmpty()) {
                for (Instance instance : instances) {
                    if (instance.getImageId().equals(amiId)) {
                        String subId = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc()
                                        .findSubnetForName(subnetName).getId();
                        if (instance.getSubnetId() != null
                            && instance.getSubnetId().equals(subId)
                            && verifyElasticIp(instance)
                            && verifyKeyPair(instance)
                            && verifySize(instance)
                            && verifySecurityGroups(instance)) {

                            result = true;
                        }
                    }
                }
            }
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private String setupBootActions() {
        String actions = "";
        log.debug("Setting up Boot Actions");
        if (getBootActions() != null) {
            getBootActions().setPlatform(platform);
            getBootActions().create();
            actions = getBootActions().getUserData();
            actions = context.resolve(actions);
        }
        log.info("Instance is being launched with following user-data script:\n\n" + actions);

        return actions;
    }

    //----------------------------------------------------------------------------------------------
    private String setupType() {
        String size = sizeType;

        if (size.indexOf(".") == -1) {
            size = null;
        }
        if (size == null || size.isEmpty()) {
            log.warn("No instance size specified. Default to m1.small");
            size = "m1.small";
        }
        else if (size.equalsIgnoreCase("t1.micro")) {
            size = "m1.small";
            log.warn("Amazon does not support t1.micro instances in Virtual Private Clouds!" +
                     "\nChanging size to " + size);
        }

        return size;
    }

    //----------------------------------------------------------------------------------------------
    private String setupKeyPair() {
        String keyPair = keyRef;

        if (keyPair == null || keyPair.isEmpty()) {
            log.warn("No key-pair specified. You may not be able to connect to instance " + name);
        }

        return keyPair;
    }

    //----------------------------------------------------------------------------------------------
    private List<BlockDeviceMapping> setupEbs() {
        List<BlockDeviceMapping> blockMaps = new ArrayList<BlockDeviceMapping>();
        if (ebsVolumes != null) {
            for (EbsTask ebs : ebsVolumes) {
                ebs.create();
                blockMaps.add(ebs.getBlockDeviceMapping());
            }
        }

        return blockMaps;
    }

    //----------------------------------------------------------------------------------------------
    private List<String> findSecurityGroups() {
        List<String> groupIds = new ArrayList<String>();
        if (getSecurityGroupRefs() != null) {
            for (SecurityGroupRefTask ref : getSecurityGroupRefs()) {
                SecurityGroupTask found = ref.fetchSecurityGroup();
                if (found != null) {
                    groupIds.add(found.getId());
                }
            }
        }

        return groupIds;
    }

    //----------------------------------------------------------------------------------------------
    private void updateMachineInfo()
    throws RemoteException {
        if (ec2Client == null) {
            throw new RemoteException("No connection to EC2");
        }
        // update the akiId and ariId with what Amazon shows
        Instance instance = helper.getInstanceById(instanceId, ec2Client);
        if (instance != null) {
            log.info("Verifying Kernel Id...");
            log.info("Expected: " + akiId);
            akiId = instance.getKernelId();
            log.info("Found: " + akiId);

            log.info("Verifying Ramdisk Id...");
            log.info("Expected: " + ariId);
            ariId = instance.getRamdiskId();
            log.info("Found: " + ariId);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void assignElasticIp(String ipToAssign)
    throws RemoteException {
        if (ec2Client == null) {
            throw new RemoteException("No connection to EC2");
        }
        if (ipToAssign == null || ipToAssign.isEmpty()) {
            if (subnetName != null && subnetId != null) {
                String allocId = helper.requestElasticIp(ec2Client);
                setElasticIpAllocId(allocId);

                String eip = helper.assignElasticIp(instanceId, allocId, ec2Client);
                setPublicIp(eip);
            }
            else {
                log.error("Cannot assign Elastic Ip to non-VPC instance");
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void registerWithLoadBalancer()
    throws RemoteException, EnvironmentCreationException {
        if (ec2Client == null) {
            throw new RemoteException("No connection to EC2");
        }

        if (loadBalancer != null && !loadBalancer.isEmpty()) {
            // we need to find the fullName of the load-balancer with name loadBalancer
            if (context != null && context.getEnvironment() != null
                    && context.getEnvironment() instanceof EnvironmentTaskAWS) {
                EnvironmentTaskAWS env = (EnvironmentTaskAWS) context.getEnvironment();
                if (env.getLoadBalancers() != null) {
                    for (LoadBalancerTask load : env.getLoadBalancers()) {
                        if (load.getName().equals(loadBalancer)) {
                            // found it
                            boolean allowed = false;
                            // check
                            if (zone != null && !zone.isEmpty()) {
                                if (load.containsZone(zone)) {
                                    allowed = true;
                                }
                                else {
                                    log.error("Load balancer " + load.getName() + " does not " +
                                            "contain " + "zone " + zone + " - could not add " +
                                            "instance " + name + " to load balancer");
                                }
                            }
                            else if (subnetName != null && !subnetName.isEmpty()) {
                                if (load.containsSubnet(subnetName)) {
                                    allowed = true;
                                }
                                else {
                                    log.error("Load balancer " + load.getName() + " does not " +
                                            "contain " + "subnet " + subnetName + " - could not " +
                                            "add instance " + name + " to load balancer");
                                }
                            }

                            if (allowed) {
                                List<String> tmp = new ArrayList<String>();
                                tmp.add(instanceId);
                                helper.updateInstancesOnLoadBalancer(load.getFullName(), tmp, true,
                                        elbClient);
                            }
                            else {
                                log.error("Instance " + name + " cannot be registered on load " +
                                        "balancer " + load.getName());
                                throw new EnvironmentCreationException("Subnet " + subnetName +
                                        " or zone " + zone + " is not associated with load " +
                                        "balancer " + load.getFullName());
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void deregisterWithLoadBalancer() {
        if (context != null && context.getEnvironment() != null
                && context.getEnvironment() instanceof EnvironmentTaskAWS) {
            EnvironmentTaskAWS env = (EnvironmentTaskAWS) context.getEnvironment();
            if (env.getLoadBalancers() != null) {
                for (LoadBalancerTask load : env.getLoadBalancers()) {
                    if (load.getName().equals(loadBalancer)) {
                        // found it, deregistering instance with load balancer
                        List<String> tmp = new ArrayList<String>();
                        tmp.add(instanceId);
                        helper.updateInstancesOnLoadBalancer(load.getFullName(), tmp, false,
                                elbClient);
                        break;
                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void waitForInstance()
    throws RemoteException, InterruptedException {
        if (instanceId != null && ec2Client != null) {
            // wait for instance to start and pass status checks
            helper.waitForState(instanceId, "running", 8, ec2Client);
            helper.waitForStatus(instanceId, "ok", 8, ec2Client);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void postStartup()
    throws RemoteException, InterruptedException {
        if (ec2Client == null) {
            throw new RemoteException("No connection to EC2");
        }
        updateMachineInfo();
        waitForInstance();

        // name Instances
        String serverName = context.getEnvironment().getName() + "-" + name;
        helper.tagInstance(instanceId, "Name", serverName, ec2Client);

        // tag the instance with the environment name
        helper.tagInstance(instanceId, "terraform.environment", context.getEnvironment().getName(),
                           ec2Client);
    }

    //----------------------------------------------------------------------------------------------
    private void startPostCreateActions(String keyPair)
    throws PostCreateException {
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
                String basePath = System.getProperty("user.home") + File.separator + ".terraform";
                String keyPairPath = basePath + File.separator + keyPair + ".pem";
                pca.setIdFile(keyPairPath);
            }
            else {
                log.warn("Trying to do PostCreateActions on instance  " + name + " ( " + instanceId
                        + " ) with no ssh key!");
            }

            pca.create();
        }
    }

    //----------------------------------------------------------------------------------------------
    private void updatePlatform() {
        // update the platform
        List<String> imageId = new ArrayList<String>();
        imageId.add(amiId);
        List<Image> images = helper.getImages(null, imageId, ec2Client);
        if (images != null) {
            Image image = images.get(0);
            platform = image.getPlatform();
        }
        if (platform == null || "".equals(platform)) {
            platform = "linux";
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create()
    throws EnvironmentCreationException {
        String size;
        String keyPair;
        boolean verified = false;

        log.debug("Creating instance " + name);

        context.setProperty("server.name", name);

        // check AWS connections
        if (ec2Client == null) {
            ec2Client = context.fetchEC2Client();
        }
        if (elbClient == null) {
            elbClient = context.fetchELBClient();
        }

        updatePlatform();

        try {
            // setup
            userData = setupBootActions();
            size = setupType();
            keyPair = setupKeyPair();

            if (!verified) {
                setId(null);
                log.info("Starting creation");

                List<String> groupIds = findSecurityGroups();
                List<BlockDeviceMapping> blockMaps = setupEbs();

                if (amiId == null) {
                    String msg = "No AMI ID specified for instance " + name + ". There is no image"
                                 + " to use.";
                    log.fatal(msg);
                    throw new EnvironmentCreationException(msg);
                }

                // launch the instance and set the Id
                instanceId = helper.launchAmi(amiId, subnetId, keyPair, size, userData, groupIds,
                        blockMaps, ariId, akiId, zone, privateIp, ec2Client);

                postStartup();

                // give instance elastic ip
                if (elasticIp) {
                    // if we send null, it will grab a new EIP and assign it
                    assignElasticIp(elasticIpAddress);
                }

                Instance instance = helper.getInstanceById(instanceId, ec2Client);

                // set public Ip
                if (zone != null && !zone.isEmpty()) {
                    setPublicIp(instance.getPublicIpAddress());
                    context.setProperty(getName() + ".public.ip", getPublicIp());
                }

                // set private Ip
                privateIp = instance.getPrivateIpAddress();
                context.setProperty(getName() + ".private.ip", getPrivateIp());

                // register with LB
                registerWithLoadBalancer();

                // do PostCreateActions
                startPostCreateActions(keyPair);
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
    throws EnvironmentDestructionException {
        if (ec2Client == null) {
            ec2Client = context.fetchEC2Client();
        }
        if (elbClient == null) {
            elbClient = context.fetchELBClient();
        }
        try {
            log.info("Shutting down instance " + getId());
            List<String> instanceIds = new ArrayList<String>();
            instanceIds.add(instanceId);

            try {
                deregisterWithLoadBalancer();
            }
            catch (AmazonServiceException e) {
                // swallow exception if we get invalidInstance
                // this will ignore the exception if you try to
                // deregister and instance that is not registered with
                // the load balancer - allowing the instance to terminate
                // completely.
                if (!e.getErrorCode().equals("InvalidInstance")) {
                    throw e;
                }
            }

            if (elasticIpAllocId != null) {
                String assocId = helper.getAssociationIdForAllocationId(elasticIpAllocId,
                                                                        ec2Client);
                if (assocId != null && !assocId.isEmpty()) {
                    helper.disassociateElasticIp(assocId, ec2Client);
                    helper.releaseElasticIp(getElasticIpAllocId(), ec2Client);
                    setElasticIpAllocId(null);
                    setPublicIp(null);
                }
                else {
                    log.error("Could not find asssociation Id for instance " + getName() +
                              "\nUnable to disassociate and release elastic ip with " +
                              "allocation id: " + elasticIpAllocId) ;
                }
            }

            if (instanceIds != null && !instanceIds.isEmpty()) {
                helper.terminateInstances(instanceIds, ec2Client);
                helper.waitForState(getId(), "terminated", 8, ec2Client);
                setId(null);
                setSubnetId(null);
            }

            if (bootActions != null) {
                bootActions.destroy();
            }

            if (secRefs != null) {
                for (SecurityGroupRefTask group : secRefs) {
                    group.destroy();
                }
            }
        }
        catch (Exception e) {
            log.error("Did not destroy instance " + name + " completely", e);
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

        // attributes
        result.setAmiId(amiId);
        result.setKernelId(akiId);
        result.setRamdiskId(ariId);
        result.setElasticIp(elasticIp);
        result.setImageSize(sizeType);
        result.setName(name);
        result.setPrivateKeyRef(keyRef);
        result.setSubnetName(subnetName);
        result.setZone(zone);

        // post create actions task
        BootActionsTask pcat = result.createBootActions();
        if (getBootActions() != null) {

            if (getBootActions().getShell() != null) {
                pcat.setShell(getBootActions().getShell());
            }

            if (getBootActions().getScript() != null) {
                for (BootActionSubTask script : getBootActions().getScript()) {
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

        PostCreateActionsTask pcat2 = result.createPostCreateActions();
        if (getPostCreateActions() != null) {
            if (getPostCreateActions().getPostCreateActions() != null) {
                for (PostCreateActionTask ssh : getPostCreateActions().getPostCreateActions()) {
                    if (ssh instanceof SshTask) {
                        SshTask sshTask = pcat2.createSsh();
                        sshTask.setCmds(((SshTask) ssh).getCmds());
                        sshTask.setPassword(ssh.getPassword());
                        sshTask.setPort(ssh.getPort());
                        sshTask.setUser(ssh.getUser());
                    }
                }
            }
        }

        // sec group refs
        if (getSecurityGroupRefs() != null) {
            for (SecurityGroupRefTask secGroup : getSecurityGroupRefs()) {
                SecurityGroupRefTask nSecGroup = null;
                if (secGroup instanceof Ec2SecurityGroupRefTask) {
                    nSecGroup = result.createEc2SecurityGroupRef();
                }
                else if (secGroup instanceof VpcSecurityGroupRefTask) {
                    nSecGroup = result.createVpcSecurityGroupRef();

                }
                else {
                    log.error("Unable to clone SecurityGroupRefTasks");
                }
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

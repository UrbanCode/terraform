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
package org.urbancode.terraform.tasks.aws.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.AssociateRouteTableResult;
import com.amazonaws.services.ec2.model.AttachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateInternetGatewayRequest;
import com.amazonaws.services.ec2.model.CreateInternetGatewayResult;
import com.amazonaws.services.ec2.model.CreateRouteRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DeleteInternetGatewayRequest;
import com.amazonaws.services.ec2.model.DeleteNetworkInterfaceRequest;
import com.amazonaws.services.ec2.model.DeleteRouteRequest;
import com.amazonaws.services.ec2.model.DeleteRouteTableRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSubnetRequest;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysResult;
import com.amazonaws.services.ec2.model.DescribeNetworkInterfacesRequest;
import com.amazonaws.services.ec2.model.DescribeNetworkInterfacesResult;
import com.amazonaws.services.ec2.model.DescribeRouteTablesRequest;
import com.amazonaws.services.ec2.model.DescribeRouteTablesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DetachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.DetachNetworkInterfaceRequest;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.DisassociateRouteTableRequest;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.NetworkInterface;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.Route;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateAppCookieStickinessPolicyRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLBCookieStickinessPolicyRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

public class AWSHelper {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    Logger log = Logger.getLogger(AWSHelper.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public AWSHelper() {

    }

    //----------------------------------------------------------------------------------------------
    /**
     * This will request a new elastic IP from Amazon and return the AllocationId
     * associated with that new elasticIp.
     *
     * @param ec2Client
     * @return allocationId
     */
    public String requestElasticIp(AmazonEC2 ec2Client) {
        String result = null;;

        AllocateAddressRequest allReq = new AllocateAddressRequest().withDomain(DomainType.Vpc);
        AllocateAddressResult allRes = ec2Client.allocateAddress(allReq);
        result = allRes.getAllocationId();

        log.info("Elastic Ip allocated: " + allRes.getPublicIp());
        log.info("Allocation Id: " + result);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Assigns an elasticIp to an instance via the allocationId. Will return the public IP.
     *
     * @param instanceId
     * @param allocationId
     * @param ec2Client
     * @return publicIp
     */
    public String assignElasticIp(String instanceId, String allocationId, AmazonEC2 ec2Client) {
        AssociateAddressRequest assAddReq = new AssociateAddressRequest()
                                                .withInstanceId(instanceId)
                                                .withAllocationId(allocationId);
        AssociateAddressResult res = ec2Client.associateAddress(assAddReq);

        DescribeAddressesRequest req = new DescribeAddressesRequest().withAllocationIds(allocationId);
        String publicIp = ec2Client.describeAddresses(req).getAddresses().get(0).getPublicIp();

        log.info("Ip ( " + publicIp + " ) assigned to:" + instanceId);

        return publicIp;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Releases an elastic IP with the given allocation id.
     *
     * @param allocationId
     * @param ec2Client
     */
    public void releaseElasticIp(String allocationId, AmazonEC2 ec2Client) {
        ReleaseAddressRequest request = new ReleaseAddressRequest().withAllocationId(allocationId);
        ec2Client.releaseAddress(request);
        log.info("Elastic IP with allocation ID = " + allocationId + " released.");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * removes the association of the elasticIp from the instance. This is done automatically by
     * amazon when an instance is terminated, there is a few seconds delay.
     *
     * @param associationId
     * @param ec2Client
     */
    public void disassociateElasticIp(String associationId, AmazonEC2 ec2Client) {
        DisassociateAddressRequest request = new DisassociateAddressRequest()
                                                  .withAssociationId(associationId);
        ec2Client.disassociateAddress(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Waits for the instance to reach the specified state (e.g. running, terminated, etc)
     * The timeout is specified in minutes.
     *
     * @param instanceId
     * @param expectedState
     * @param timeout
     * @param ec2Client
     */
    public void waitForState(String instanceId, String expectedState, int timeout, AmazonEC2 ec2Client)
    throws RemoteException, InterruptedException {
        long pollInterval = 3000L;
        long timeoutInterval = timeout * 60L * 1000L;
        long start = System.currentTimeMillis();

        String state = expectedState+"a";

        log.info("Waiting for instance " + instanceId + " to reach " + expectedState + " state");
        while (!state.equals(expectedState)) {
            if (System.currentTimeMillis() - start > timeoutInterval) {
                log.fatal("Instance " + instanceId + " never reached " + expectedState);
                throw new RemoteException(
                        "Timeout while waiting for instance to hit " + expectedState + " state.");
            }
            state = getInstanceState(instanceId, ec2Client);
            Thread.sleep(pollInterval);
        }
        log.info("Instance " + instanceId + " is now in " + state + " state");
    }

    //----------------------------------------------------------------------------------------------
    public String getInstanceState(String instanceId, AmazonEC2 ec2Client) {
        String state = null;

        Instance instance = getInstanceById(instanceId, ec2Client);
        if (instance != null) {
            state = instance.getState().getName();
        }
        else {
            log.error("Instance is null");
        }

        return state;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Waits for an instance to hit a specific status.
     * Assumes that expected status is either 'ok' or 'impaired'
     *
     * @param instanceId
     * @param expectedStatus
     * @param timeout
     * @param ec2Client
     */
    public void waitForStatus(String instanceId, String expectedStatus, int timeout, AmazonEC2 ec2Client)
    throws RemoteException, InterruptedException {
        long pollInterval = 3000L;
        long timeoutInterval = timeout * 60L * 1000L;
        long start = System.currentTimeMillis();

        String status = getInstanceStatus(instanceId, ec2Client);
        String oppositeStatus = "impaired";
        if (expectedStatus.equalsIgnoreCase("impaired")) {
            oppositeStatus = "ok";
        }

        log.info("Waiting for instance status to reach: " + expectedStatus);

        while (!expectedStatus.equals(status) || oppositeStatus.equals(status)) {

            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException(
                        "Timeout while waiting for instance to hit " + expectedStatus + " status.");
            }
            if (status != null) {
                status = getInstanceStatus(instanceId, ec2Client);
                Thread.sleep(pollInterval);
            }
            else {
                log.error("Instance has null status!");
                return;
            }
        }
        log.info("Instance hit " + status + " status. Stopping now.");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Finds and returns the status of an instance given its instanceId
     *
     * @param instanceId
     * @param ec2Client
     * @return status
     */
    public String getInstanceStatus(String instanceId, AmazonEC2 ec2Client) {
        String status = null;

        DescribeInstanceStatusRequest request = new DescribeInstanceStatusRequest()
                                                .withInstanceIds(instanceId);
        DescribeInstanceStatusResult result = ec2Client.describeInstanceStatus(request);

        if (result.getInstanceStatuses() != null && !result.getInstanceStatuses().isEmpty()) {
            // get first instanceStatus since we only requested 1 instance
            status = result.getInstanceStatuses().get(0).getInstanceStatus().getStatus();
        }
        log.info("Instance (" + instanceId + ") found with status (" + status +")");

        return status;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * This will call out to amazon to retrieve and instance object based on the given
     * instanceId.
     *
     * @param instanceId
     * @param ec2Client
     * @return Instance
     */
    public Instance getInstanceById(String instanceId, AmazonEC2 ec2Client) {
        List<String> ids = new ArrayList<String>();
        ids.add(instanceId);
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(ids);
        DescribeInstancesResult result = ec2Client.describeInstances(request);

        Instance instance = null;
        List<Reservation> reservations = result.getReservations();

        if (reservations != null && !reservations.isEmpty()) {
            // there should only be 1 reservation and 1 instance
            instance = reservations.get(0).getInstances().get(0);
            log.info("Instance found: " + instance.getInstanceId());
        }
        else {
            log.error("No instance found with id: " + instanceId);
        }

        return instance;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Launches a single instance with given parameters.
     * The REQUIRED parameters are amiId;
     * You should always pass in a keyPair also, unless you know exactly what you are doing. Chances
     * are that you won't be able to get into your instance and it will be useless.
     * If groups is null, the instance will be launched with the default security group.
     * Subnet is only required if you're launching into a VPC.
     *
     * This will return the instanceId of the instance launched.
     *
     * @param amiId
     * @param subnetId
     * @param keyPair
     * @param size
     * @param userData
     * @param groups
     * @param ec2Client
     * @return instanceId
     */
    public String launchAmi(String amiId, String subnetId, String keyPair, String size, String userData, List<String> groups, List<BlockDeviceMapping> blockMaps, AmazonEC2 ec2Client) {
        String instanceId = null;
        RunInstancesRequest request =  new RunInstancesRequest()
                                            .withImageId(amiId)
                                            .withMinCount(1)
                                            .withMaxCount(1);
        if (subnetId != null) {
            request = request.withSubnetId(subnetId);
        }
        if (keyPair != null) {
            request = request.withKeyName(keyPair);
        }
        if (size != null) {
            request = request.withInstanceType(size);
        }
        if (userData != null) {
            request = request.withUserData(Base64.encodeBase64String(userData.getBytes()));
        }
        if (groups != null && !groups.isEmpty()) {
            request = request.withSecurityGroupIds(groups);
        }
        if (blockMaps != null && !blockMaps.isEmpty()) {
            request = request.withBlockDeviceMappings(blockMaps);
        }

        RunInstancesResult result = ec2Client.runInstances(request);

        List<Instance> instances = result.getReservation().getInstances();

        if (instances == null) {
            instanceId = null;
            log.error("List of instances is null!");
        }
        else if (instances.size() == 0) {
            instanceId = null;
            log.error("List of instances is empty!");
        }
        else if (instances.size() == 1) {
            instanceId = instances.get(0).getInstanceId();
            log.info("Created instance with Id: " + instanceId );
        }
        else if (instances.size() > 1 ) {
            log.error("Too many instances! This is not supported!");
        }

        return instanceId;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Will send a request to amazon to terminate all instances in the given list.
     *
     * @param instanceIds
     * @param ec2Client
     */
    public void terminateInstances(List<String> instanceIds, AmazonEC2 ec2Client) {
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceIds);
        ec2Client.terminateInstances(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Detaches the given internet gateway from the given vpc
     *
     * @param gatewayId
     * @param vpcId
     * @param ec2Client
     */
    public void detachGateway(String gatewayId, String vpcId, AmazonEC2 ec2Client) {
        DetachInternetGatewayRequest request = new DetachInternetGatewayRequest()
                                                    .withInternetGatewayId(gatewayId)
                                                    .withVpcId(vpcId);
        ec2Client.detachInternetGateway(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Cleans up any elastic ips that are not associated with anything (via associationId).
     * The addresses are released.
     *
     * @param ec2Client
     */
    public void cleanupElasticIps(AmazonEC2 ec2Client) {
        DescribeAddressesResult result = ec2Client.describeAddresses();
        List<Address> addresses = result.getAddresses();
        if (addresses != null) {
            for (Address address : addresses) {
                if (address.getAssociationId() != null && !address.getAssociationId().equals("")) {
                    ReleaseAddressRequest request = new ReleaseAddressRequest()
                                                        .withAllocationId(address.getAllocationId());
                    ec2Client.releaseAddress(request);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public void waitForPublicAddresses(AmazonEC2 ec2Client) {
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Updates the instances registered on the given load balancer. You can either register or
     *  deregister a list of instances given their ids.
     * @param loadBalancerName
     * @param instanceIds
     * @param doRegister
     * @param lbClient
     * @return
     */
    public List<String> updateInstancesOnLoadBalancer(String loadBalancerName, List<String> instanceIds, boolean doRegister, AmazonElasticLoadBalancing lbClient) {
        List<String> updatedIds = new ArrayList<String>();

        List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
        if (instanceIds != null) {
            for (String instanceId : instanceIds) {
                com.amazonaws.services.elasticloadbalancing.model.Instance instance = new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId);
                instances.add(instance);
            }

            List<com.amazonaws.services.elasticloadbalancing.model.Instance> updatedInstances;

            if (doRegister) {
                updatedInstances = registerInstancesLB(loadBalancerName, instances, lbClient);
            }
            else {
                updatedInstances = deregisterInstancesLB(loadBalancerName, instances, lbClient);
            }


            if (updatedInstances != null) {
                for (com.amazonaws.services.elasticloadbalancing.model.Instance instance : instances) {
                    updatedIds.add(instance.getInstanceId());
                }
            }
        }

        return updatedIds;
    }

    //----------------------------------------------------------------------------------------------
    public List<com.amazonaws.services.elasticloadbalancing.model.Instance> deregisterInstancesLB(String loadBalancerName, List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances, AmazonElasticLoadBalancing lbClient) {
        DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest()
                                                                  .withInstances(instances)
                                                                  .withLoadBalancerName(loadBalancerName);
        DeregisterInstancesFromLoadBalancerResult result = lbClient.deregisterInstancesFromLoadBalancer(request);
        return result.getInstances();
    }

    //----------------------------------------------------------------------------------------------
    public List<com.amazonaws.services.elasticloadbalancing.model.Instance> registerInstancesLB(String loadBalancerName, List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances, AmazonElasticLoadBalancing lbClient) {
        List<com.amazonaws.services.elasticloadbalancing.model.Instance> updatedInstances = null;
        if (instances != null) {
            RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest()
            .withInstances(instances)
            .withLoadBalancerName(loadBalancerName);
            RegisterInstancesWithLoadBalancerResult result = lbClient.registerInstancesWithLoadBalancer(request);
            updatedInstances = result.getInstances();
        }

        return updatedInstances;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Launches a load balancer with the given name, subnets, security groups, and listeners.
     * NOTE** if you have multiple subnets, each must be in a DIFFERENT availability zone!
     *          you can only have 1 subnet per availability zone
     *
     * @param loadBalancerName - must be unique!!
     * @param subnets
     * @param secGroups
     * @param listeners - you can create these locally. holds info on ports/protocols
     * @param lbClient - AmazonElasticLoadBalancing
     * @return DNSName - the DNS name of the newly created load balancer
     * @throws NullPointerException
     */
    public String launchLoadBalancer(String loadBalancerName, List<String> subnets,
                                        List<String> secGroups, List<Listener> listeners,
                                        AmazonElasticLoadBalancing lbClient)
    throws NullPointerException {
        CreateLoadBalancerRequest request = new CreateLoadBalancerRequest()
                                                .withLoadBalancerName(loadBalancerName);
        if (subnets != null && !subnets.isEmpty()) {
            request = request.withSubnets(subnets);
        }
        else {
            throw new NullPointerException("List of subnets must not be null!");
        }

        if (listeners != null && !listeners.isEmpty()) {
            request = request.withListeners(listeners);
        }
        else {
            throw new NullPointerException("List of Listeners must not be null!");
        }

        if (secGroups != null && !secGroups.isEmpty()) {
            request = request.withSecurityGroups(secGroups);
        }

        CreateLoadBalancerResult result = lbClient.createLoadBalancer(request);
        return result.getDNSName();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Retrieves the AssocaitionId (given when you associate an elastic IP with an instance)
     *  from the AllocationId (given when you request an elastic IP)
     *
     * @param allocId
     * @param ec2Client
     * @return
     */
    public String getAssociationIdForAllocationId(String allocId, AmazonEC2 ec2Client) {
        String assocId = null;
        DescribeAddressesRequest request = new DescribeAddressesRequest().withAllocationIds(allocId);
        DescribeAddressesResult result = ec2Client.describeAddresses(request);
        List<Address> addresses = result.getAddresses();
        if (addresses != null & !addresses.isEmpty()) {
            if (addresses.size() > 1) {
                log.error("Found more than one Address for allocationId ( " + allocId + " ) !");
            }
            assocId = addresses.get(0).getAssociationId();
        }
        return assocId;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param availabilityZone -
     * @param size - size (in Gb) of the volume to be created. Must be larger than snapshot if used
     * @param snapshotId - optional if you want to create from a snapshot
     * @param ec2Client
     * @return volumeId - the id of the newly created volume
     */
    public String createEbsVolume(String availabilityZone, int size, String snapshotId, AmazonEC2 ec2Client) {
        CreateVolumeRequest request = new CreateVolumeRequest()
                                           .withAvailabilityZone(availabilityZone)
                                           .withSize(size);

        // Only create from a snapshot if the snapshotId is not null, otherwise make new volume
        if (snapshotId != null && !snapshotId.equals("")) {
            request = request.withSnapshotId(snapshotId);
        }

        CreateVolumeResult result = ec2Client.createVolume(request);
        String volumeId = result.getVolume().getVolumeId();

        return volumeId;
    }

    //----------------------------------------------------------------------------------------------
    public void deleteEbsVolume(String volumeId, AmazonEC2 ec2Client) {
        DeleteVolumeRequest request = new DeleteVolumeRequest()
                                           .withVolumeId(volumeId);
        ec2Client.deleteVolume(request);
    }

    //----------------------------------------------------------------------------------------------
    public void attachEbsVolumeToInstance(String volumeId, String instanceId, String device, AmazonEC2 ec2Client) {
        AttachVolumeRequest request = new AttachVolumeRequest()
                                           .withInstanceId(instanceId)
                                           .withVolumeId(volumeId)
                                           .withDevice(device);
        AttachVolumeResult result = ec2Client.attachVolume(request);
    }

    //----------------------------------------------------------------------------------------------
    public void detachEbsVolumeFromInstance(String volumeId, String instanceId, String device,
                                                 boolean force, AmazonEC2 ec2Client) {
        DetachVolumeRequest request = new DetachVolumeRequest()
                                           .withDevice(device)
                                           .withInstanceId(instanceId)
                                           .withVolumeId(volumeId)
                                           .withForce(force);
        DetachVolumeResult result = ec2Client.detachVolume(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Deletes the route corresponding to the given info
     *
     * @param routeTableId
     * @param destCidr
     * @param ec2Client
     */
    public void deleteRoute(String routeTableId, String destCidr, AmazonEC2 ec2Client) {
        DeleteRouteRequest request = new DeleteRouteRequest()
                                          .withDestinationCidrBlock(destCidr)
                                          .withRouteTableId(routeTableId);
        ec2Client.deleteRoute(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates a route with given info. Attach Id can be either an instance (as a NAT) or an
     *  gateway.
     *
     * @param routeTableId
     * @param destCidr
     * @param attachId
     * @param ec2Client
     */
    public void createRoute(String routeTableId, String destCidr, String attachId, AmazonEC2 ec2Client) {
        CreateRouteRequest request = new CreateRouteRequest()
                                          .withDestinationCidrBlock(destCidr)
                                          .withRouteTableId(routeTableId);
        if (attachId.startsWith("i-")) {
            request = request.withInstanceId(attachId);
        }
        else if (attachId.startsWith("igw-")) {
            request = request.withGatewayId(attachId);
        }
        ec2Client.createRoute(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates an internetGateway and returns the id it was created with.
     *
     * @param ec2Client
     * @return InternetGatewayId
     */
    public String createInternetGateway(AmazonEC2 ec2Client) {
        CreateInternetGatewayRequest request = new CreateInternetGatewayRequest();
        CreateInternetGatewayResult result = ec2Client.createInternetGateway(request);
        String gatewayId = result.getInternetGateway().getInternetGatewayId();

        return gatewayId;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Deletes the given internetGateway. Does not detach the gateway if it is attached.
     *
     * @param gatewayId
     * @param ec2Client
     */
    public void deleteInternetGateway(String gatewayId, AmazonEC2 ec2Client) {
        // TODO check if gateway is attached to anything
        DeleteInternetGatewayRequest request = new DeleteInternetGatewayRequest()
                                                    .withInternetGatewayId(gatewayId);
        ec2Client.deleteInternetGateway(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Attaches the given gateway to the given Vpc.
     *
     * @param gatewayId
     * @param vpcId
     * @param ec2Client
     */
    public void attachInternetGatewayToVpc(String gatewayId, String vpcId, AmazonEC2 ec2Client) {
        AttachInternetGatewayRequest request = new AttachInternetGatewayRequest()
                                                    .withInternetGatewayId(gatewayId)
                                                    .withVpcId(vpcId);
        ec2Client.attachInternetGateway(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Detaches the given internetGateway from the given Vpc. Does not check for any
     *  public mapped addresses before detaching.
     *
     * @param gatewayId
     * @param vpcId
     * @param ec2Client
     */
    public void detachInternetGatewayFromVpc(String gatewayId, String vpcId, AmazonEC2 ec2Client) {
        DetachInternetGatewayRequest request = new DetachInternetGatewayRequest()
                                                    .withInternetGatewayId(gatewayId)
                                                    .withVpcId(vpcId);
        ec2Client.detachInternetGateway(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Created the sticky policy used by the load balancer, if you specify an appCookieName then
     * it will use the App policy, otherwise it will create a LB policy on the LB.
     *
     * @param loadBalancerName
     * @param policyName
     * @param appCookieName - leave NULL if you want to use LB sticky policy
     * @param cookieExp - only used for LB sticky policy
     * @param elbClient
     */
    public void createStickyPolicy(String loadBalancerName, String policyName, String appCookieName,
                                       long cookieExp, AmazonElasticLoadBalancing elbClient) {
        if (appCookieName != null && appCookieName != "") {
            CreateAppCookieStickinessPolicyRequest request = new CreateAppCookieStickinessPolicyRequest()
                                                                  .withCookieName(appCookieName)
                                                                  .withPolicyName(policyName)
                                                                  .withLoadBalancerName(loadBalancerName);
            elbClient.createAppCookieStickinessPolicy(request);
        }
        else {
            CreateLBCookieStickinessPolicyRequest request = new CreateLBCookieStickinessPolicyRequest()
                                                                 .withLoadBalancerName(loadBalancerName)
                                                                 .withPolicyName(policyName)
                                                                 .withCookieExpirationPeriod(cookieExp);
            elbClient.createLBCookieStickinessPolicy(request);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Sets up the heath check on the given load balancer.
     * The target should be in format  protocol:port/path  (e.g. http:8080/index.html)
     *
     * @param loadBalancerName
     * @param target
     * @param healthyThresh
     * @param unhealthyThresh
     * @param interval
     * @param timeout
     * @param elbClient
     */
    public void setupHealthCheck(String loadBalancerName, String target, int healthyThresh,
                                    int unhealthyThresh, int interval, int timeout,
                                    AmazonElasticLoadBalancing elbClient) {
        // TODO - validate format of target
        HealthCheck hCheck = new HealthCheck()
                                 .withTarget(target)
                                 .withHealthyThreshold(healthyThresh)
                                 .withUnhealthyThreshold(unhealthyThresh)
                                 .withInterval(interval)
                                 .withTimeout(timeout);

        ConfigureHealthCheckRequest healthRequest = new ConfigureHealthCheckRequest()
                                 .withHealthCheck(hCheck)
                                 .withLoadBalancerName(loadBalancerName);

        ConfigureHealthCheckResult healthResult = elbClient.configureHealthCheck(healthRequest);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Gets a list of all RouteTables or all RouteTables listed in routeTableIds from Amazon.
     * You can leave the list of ids empty or null to get all route tables.
     *
     * @param routeTableIds - leave this null or empty to get all RouteTables
     * @param ec2Client
     * @return RouteTables - a List of RouteTables found
     */
    public List<RouteTable> describeRouteTables(List<String> routeTableIds, AmazonEC2 ec2Client) {
        DescribeRouteTablesRequest request = new DescribeRouteTablesRequest();

        if (routeTableIds != null && !routeTableIds.isEmpty()) {
            request = request.withRouteTableIds(routeTableIds);
        }
        DescribeRouteTablesResult result = ec2Client.describeRouteTables(request);

        return result.getRouteTables();
    }

    //----------------------------------------------------------------------------------------------
    public String associateRouteTableWithSubnet(String routeTableId, String subnetId, AmazonEC2 ec2Client) {
        AssociateRouteTableRequest request = new AssociateRouteTableRequest()
                                                  .withRouteTableId(routeTableId)
                                                  .withSubnetId(subnetId);
        AssociateRouteTableResult result = ec2Client.associateRouteTable(request);
        String associationId = result.getAssociationId();

        return associationId;
    }

    //----------------------------------------------------------------------------------------------
    public String createRouteTable(String vpcId, AmazonEC2 ec2Client) {
        CreateRouteTableRequest request = new CreateRouteTableRequest()
                                               .withVpcId(vpcId);
        CreateRouteTableResult result = ec2Client.createRouteTable(request);
        String routeTableId = result.getRouteTable().getRouteTableId();

        return routeTableId;
    }

    //----------------------------------------------------------------------------------------------
    public void deleteRouteTable(String routeTableId, AmazonEC2 ec2Client) {
        DeleteRouteTableRequest request = new DeleteRouteTableRequest()
                                               .withRouteTableId(routeTableId);
        ec2Client.deleteRouteTable(request);
    }

    //----------------------------------------------------------------------------------------------
    public void disassociateRouteTableFromSubnet(String associationId, AmazonEC2 ec2Client) {
        DisassociateRouteTableRequest request = new DisassociateRouteTableRequest()
                                                     .withAssociationId(associationId);
        ec2Client.disassociateRouteTable(request);
    }

    //----------------------------------------------------------------------------------------------
    public void createRuleForSecurityGroup(String groupId, String protocol, int startPort,
                                                int endPort, String cidr, boolean inbound,
                                                AmazonEC2 ec2Client) {
        // protocol should be lowercase
        protocol = protocol.toLowerCase();

        // create container for request
        // we need to use IpPermission object here because the other (old) way
        // is depreciated and no longer works (but it's still in the code?)
        IpPermission perm = new IpPermission().withFromPort(startPort)
                                               .withToPort(endPort)
                                               .withIpProtocol(protocol)
                                               .withIpRanges(cidr);
        if (inbound) {
            // inbound rule
            AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                                                                .withGroupId(groupId)
                                                                .withIpPermissions(perm);
            ec2Client.authorizeSecurityGroupIngress(request);
        }
        else {
            // outbound rule
            AuthorizeSecurityGroupEgressRequest request = new AuthorizeSecurityGroupEgressRequest()
                                                                .withGroupId(groupId)
                                                                .withIpPermissions(perm);
            ec2Client.authorizeSecurityGroupEgress(request);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void deleteRuleForSecurityGroup(String groupId, String protocol, int startPort,
                                                int endPort, String cidr, boolean inbound,
                                                AmazonEC2 ec2Client) {

        IpPermission perm = new IpPermission().withFromPort(startPort)
                                               .withToPort(endPort)
                                               .withIpProtocol(protocol)
                                               .withIpRanges(cidr);

        if (inbound) {
            RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest()
                                                             .withGroupId(groupId)
                                                             .withIpPermissions(perm);
            ec2Client.revokeSecurityGroupIngress(request);
        }
        else {
            RevokeSecurityGroupEgressRequest request = new RevokeSecurityGroupEgressRequest()
                                                            .withGroupId(groupId)
                                                            .withIpPermissions(perm);
            ec2Client.revokeSecurityGroupEgress(request);
        }
    }

    //----------------------------------------------------------------------------------------------
    public String createSubnet(String vpcId, String cidr, String zone, AmazonEC2 ec2Client) {
        CreateSubnetRequest request = new CreateSubnetRequest()
                                           .withVpcId(vpcId)
                                           .withCidrBlock(cidr)
                                           .withAvailabilityZone(zone);
        CreateSubnetResult result = ec2Client.createSubnet(request);
        String subnetId = result.getSubnet().getSubnetId();

        return subnetId;
    }

    //----------------------------------------------------------------------------------------------
    public void deleteSubnet(String subnetId, AmazonEC2 ec2Client) {
        DeleteSubnetRequest request = new DeleteSubnetRequest()
                                           .withSubnetId(subnetId);
        ec2Client.deleteSubnet(request);
    }

    //----------------------------------------------------------------------------------------------
    public List<Subnet> describeSubnets(List<String> subnetIds, AmazonEC2 ec2Client) {
        DescribeSubnetsRequest request = new DescribeSubnetsRequest();

        if (subnetIds != null && !subnetIds.isEmpty()) {
            request = request.withSubnetIds(subnetIds);
        }
        DescribeSubnetsResult result = ec2Client.describeSubnets(request);

        return result.getSubnets();
    }

    //----------------------------------------------------------------------------------------------
    public List<SecurityGroup> describeSecurityGroups(List<String> groupIds, AmazonEC2 ec2Client) {
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();

        if (groupIds != null && !groupIds.isEmpty()) {
            request = request.withGroupIds(groupIds);
        }
        DescribeSecurityGroupsResult result = ec2Client.describeSecurityGroups(request);

        return result.getSecurityGroups();
    }

    //----------------------------------------------------------------------------------------------
    public String createSecurityGroup(String groupName, String vpcId, String descr, AmazonEC2 ec2Client) {
        CreateSecurityGroupRequest request = new CreateSecurityGroupRequest()
                                                  .withGroupName(groupName)
                                                  .withVpcId(vpcId)
                                                  .withDescription(descr);
        CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);

        return result.getGroupId();
    }

    //----------------------------------------------------------------------------------------------
    public void deleteSecurityGroup(String groupId, AmazonEC2 ec2Client) {
        DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest()
                                                  .withGroupId(groupId);
        ec2Client.deleteSecurityGroup(request);
    }

    //----------------------------------------------------------------------------------------------
    public String createVpc(String cidr, AmazonEC2 ec2Client) {
        CreateVpcRequest request = new CreateVpcRequest().withCidrBlock(cidr);
        CreateVpcResult result = ec2Client.createVpc(request);
        String vpcId = result.getVpc().getVpcId();

        return vpcId;
    }

    //----------------------------------------------------------------------------------------------
    public void deleteVpc(String vpcId, AmazonEC2 ec2Client) {
        DeleteVpcRequest request = new DeleteVpcRequest().withVpcId(vpcId);
        ec2Client.deleteVpc(request);
    }

    //----------------------------------------------------------------------------------------------
    public List<InternetGateway> describeInternetGateways(List<String> gatewayIds, AmazonEC2 ec2Client) {
        DescribeInternetGatewaysRequest request = new DescribeInternetGatewaysRequest();

        if (gatewayIds != null && !gatewayIds.isEmpty()) {
            request = request.withInternetGatewayIds(gatewayIds);
        }
        DescribeInternetGatewaysResult result = ec2Client.describeInternetGateways(request);

        return result.getInternetGateways();
    }

    //----------------------------------------------------------------------------------------------
    public List<Instance> describeInstances(List<String> instanceIds, AmazonEC2 ec2Client) {
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        if (instanceIds != null && !instanceIds.isEmpty()) {
            request = request.withInstanceIds(instanceIds);
        }
        DescribeInstancesResult result = ec2Client.describeInstances(request);

        List<Instance> instances = new ArrayList<Instance>();
        List<Reservation> reses = result.getReservations();
        if (reses != null) {
            for (Reservation res : result.getReservations()) {
                if (res.getInstances() != null && !res.getInstances().isEmpty()) {
                    for (Instance instance : res.getInstances()) {
                        instances.add(instance);
                    }
                }
            }
        }
        return instances;
    }

    //----------------------------------------------------------------------------------------------
    public List<Route> describeRoutes(List<String> routeIds, String routeTableId, AmazonEC2 ec2Client) {
        List<String> routeTableIds = new ArrayList<String>();
        routeTableIds.add(routeTableId);
        List<RouteTable> tables = describeRouteTables(routeTableIds, ec2Client); // only returns 1 table
        RouteTable table = tables.get(0);
        table.getRoutes().get(0);
        return table.getRoutes();
    }

    //----------------------------------------------------------------------------------------------
    public List<NetworkInterface> describeNetworkInterfaces(List<String> interfaceIds, String vpcId, AmazonEC2 ec2Client) {
        DescribeNetworkInterfacesRequest request = new DescribeNetworkInterfacesRequest();

        if (interfaceIds != null) {
            request = request.withNetworkInterfaceIds(interfaceIds);
        }
        if (vpcId != null && !vpcId.equals("")) {
            Filter vpcFilter = new Filter().withName("vpc-id").withValues(vpcId);
            request = request.withFilters(vpcFilter);
        }

        DescribeNetworkInterfacesResult result = ec2Client.describeNetworkInterfaces(request);

        return result.getNetworkInterfaces();
    }

    //----------------------------------------------------------------------------------------------
    public void detachNetworkInterfaces(List<String> attachIds, AmazonEC2 ec2Client) {
        if (attachIds != null) {
            for (String attachId : attachIds) {
                DetachNetworkInterfaceRequest request = new DetachNetworkInterfaceRequest()
                                                             .withAttachmentId(attachId);
                ec2Client.detachNetworkInterface(request);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void deleteNetworkInterfaces(List<String> interfaceIds, AmazonEC2 ec2Client) {
        if (interfaceIds != null) {
            for (String interfaceId : interfaceIds) {
                DeleteNetworkInterfaceRequest request = new DeleteNetworkInterfaceRequest()
                                                             .withNetworkInterfaceId(interfaceId);
                ec2Client.deleteNetworkInterface(request);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void tagInstance(String instanceId, String tag, String value, AmazonEC2 ec2Client) {
        CreateTagsRequest request = new CreateTagsRequest();
        request = request.withResources(instanceId)
                         .withTags(new Tag(tag, value));
        ec2Client.createTags(request);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * This will return the owner id of the given connection to EC2 by describing the "default"
     * security group. This is the only reliable way of getting the owner id since there is
     * no way to describe the value in the SDK
     *
     * @param ec2Client
     * @return ownerId - the ID of the current user (as determined by the credentials the
     *                    ec2Client was made with)
     */
    public String getCurrentOwnerId(AmazonEC2 ec2Client) {
        String ownerId = null;

        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest()
                                                     .withGroupNames("default");
        DescribeSecurityGroupsResult result = ec2Client.describeSecurityGroups(request);

        if (result != null) {
            // there should always be a "default" security group, but we'll check anyways
            if (result.getSecurityGroups() != null) {
                ownerId = result.getSecurityGroups().get(0).getOwnerId();
            }
        }

        return ownerId;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     *
     * @param ownerId - if null, will grab current user
     * @param ec2Client
     * @return images - returns a List<Image> of all images for that owner
     */
    public List<Image> getImages(String ownerId, AmazonEC2 ec2Client) {
        List<Image> images = new ArrayList<Image>();
        if (ownerId == null || "".equals(ownerId)) {
            ownerId = getCurrentOwnerId(ec2Client);
        }

        DescribeImagesRequest request = new DescribeImagesRequest()
                                             .withOwners(ownerId);
        DescribeImagesResult result = ec2Client.describeImages(request);

        images = result.getImages();

        return images;
    }

    //----------------------------------------------------------------------------------------------
    public String getPrivateIp(String instanceId, AmazonEC2 ec2Client) {
        String privateIp = null;

        Instance instance = this.getInstanceById(instanceId, ec2Client);
        if (instance != null) {
            privateIp = instance.getPrivateIpAddress();
        }

        return privateIp;
    }
}

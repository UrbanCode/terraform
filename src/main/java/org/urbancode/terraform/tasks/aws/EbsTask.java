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
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.EbsBlockDevice;

public class EbsTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(EbsTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;

    private String name;
    private String deviceName;
    private String snapshotId;
    private String volumeId;
    private String instanceId;
    private String zone;

    private int volumeSize;

    private boolean persist;
    private boolean onLaunch;
    private boolean mount;

    private BlockDeviceMapping blockMap;

    //----------------------------------------------------------------------------------------------
    public EbsTask(ContextAWS context) {
        this.context = context;
        helper = new AWSHelper();
    }

    //----------------------------------------------------------------------------------------------
    public void setZone(String zone) {
        this.zone = zone;
    }

    //----------------------------------------------------------------------------------------------
    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    //----------------------------------------------------------------------------------------------
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    //----------------------------------------------------------------------------------------------
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    //----------------------------------------------------------------------------------------------
    public void setVolumeSize(int volumeSize) {
        this.volumeSize = volumeSize;
    }

    //----------------------------------------------------------------------------------------------
    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    //----------------------------------------------------------------------------------------------
    protected BlockDeviceMapping getBlockDeviceMapping() {
        return blockMap;
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public String getVolumeId() {
        return volumeId;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getPersist() {
        return persist;
    }

    //----------------------------------------------------------------------------------------------
    public String getDeviceName() {
        return deviceName;
    }

    //----------------------------------------------------------------------------------------------
    public String getSnapshotId() {
        return snapshotId;
    }

    //----------------------------------------------------------------------------------------------
    public int getVolumeSize() {
        return volumeSize;
    }

    //----------------------------------------------------------------------------------------------
    public boolean isVerified() {
        boolean verified = false;

        // TODO - verify this

        return verified;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates the EBS volume on instance launch. Only the data structures are required as we send
     * then with the RunInstancesRequest.
     */
    private void createOnLaunch() {
        // These AWS data structures are needed when launching an ami/running an instance.
        // this will need to be handled differently if creating/attaching an EBS volume
        //  to an already existing instance.
        // PROBLEM: we don't have the volumeId for this new ebs volume
        EbsBlockDevice block = new EbsBlockDevice()
                                    .withDeleteOnTermination(persist)
                                    .withSnapshotId(snapshotId)
                                    .withVolumeSize(volumeSize);

        blockMap = new BlockDeviceMapping()
                                           .withDeviceName(deviceName)
                                           .withEbs(block)
                                           .withVirtualName(name);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates the EBS volume after the instance has launched. A connection to Amazon EC2 is needed.
     * The volume is created, and then attached to the instance.
     */
    private void createPostLaunch() {
        if (ec2Client == null) {
            ec2Client = context.fetchEC2Client();
        }

        if (!isVerified()) {
            log.info("Creating EBS Volume...");
            // TODO - null checks
            volumeId = helper.createEbsVolume(zone, volumeSize, snapshotId, ec2Client);
            log.info("EBS Volume " + name + " created with id: " + volumeId);

            log.info("Attaching volume...");
            // TODO - null checks
            helper.attachEbsVolumeToInstance(volumeId, instanceId, deviceName, ec2Client);
            log.info("EBS Volume " + volumeId + " attached to instance " + instanceId + " at " +
                    deviceName);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     */
    private void mountVolume() {
        // TODO
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     */
    private void destroyOnLaunch() {
        // TODO
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     */
    private void destroyPostLaunch() {
        if (ec2Client == null) {
            ec2Client = context.fetchEC2Client();
        }

        // detach with force
        log.info("Detaching volume...");
        helper.detachEbsVolumeFromInstance(volumeId, instanceId, deviceName, true, ec2Client);
        log.info("Volume " + volumeId + " detached from instance " + instanceId + " at " +
                deviceName);

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        if (onLaunch) {
            createOnLaunch();
        }
        else {
            createPostLaunch();
        }

        if (mount) {
            mountVolume();
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {

        // try to unmount?

        if (onLaunch) {
            destroyOnLaunch();
        }
        else {
            destroyPostLaunch();
        }

        if (!persist) {
            log.info("Deleting volume...");
            helper.deleteEbsVolume(volumeId, ec2Client);
            log.info("Volume " + volumeId + " deleted");
        }
        else {
            log.info("Volume " + volumeId + " persited in AWS");
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {
        // TODO Auto-generated method stub

    }
}

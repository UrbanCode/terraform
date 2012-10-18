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
package org.urbancode.terraform.tasks.vmware;

import java.rmi.RemoteException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.util.Path;
import org.urbancode.terraform.tasks.vmware.util.SwitchResult;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.urbancode.x2o.tasks.SubTask;
import com.vmware.vim25.HostNetworkPolicy;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.HostNetworkSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Network;

public class NetworkTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(NetworkTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String networkName;
    private String switchName;
    private String portGroupName;
    private int portCount;
    private int vlanId = 0;
    private SwitchResult vSwitch;
    private VirtualHost host = null;
    private Path hostPath = null;

    //----------------------------------------------------------------------------------------------
    public NetworkTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getNetworkName() {
        return this.networkName;
    }

    //----------------------------------------------------------------------------------------------
    public int getPortCount() {
        return this.portCount;
    }

    //----------------------------------------------------------------------------------------------
    public String getSwitchName() {
        return this.switchName;
    }

    //----------------------------------------------------------------------------------------------
    public String getPortGroupName() {
        return this.portGroupName;
    }

    //----------------------------------------------------------------------------------------------
    public int getVlanId() {
        return vlanId;
    }

    //----------------------------------------------------------------------------------------------
    public SwitchResult fetchSwitch() {
        return this.vSwitch;
    }

    //----------------------------------------------------------------------------------------------
    public void setHostPath(Path hostPath) {
        this.hostPath = hostPath;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualHost(VirtualHost host) {
        this.host = host;
    }

    //----------------------------------------------------------------------------------------------
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    //----------------------------------------------------------------------------------------------
    public void setPortCount(int portCount) {
        this.portCount = portCount;
    }

    //----------------------------------------------------------------------------------------------
    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }

    //----------------------------------------------------------------------------------------------
    public void setPortGroupName(String portGroupName) {
        this.portGroupName = portGroupName;
    }

    //----------------------------------------------------------------------------------------------
    public void setSwitch(SwitchResult vSwitch) {
        this.vSwitch = vSwitch;
    }

    //----------------------------------------------------------------------------------------------
    public void setVlanId(int vlanId) {
        this.vlanId = vlanId;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates a virtual switch and port group.
     * The virtual switch has a randomly generated name of 10 hexadecimal characters.
     * The port group has the same name as the virtual switch with the suffix "-network" appended.
     * @return a SwitchResult object storing the VI Java Network object and paths to the port group and virtual switch
     * @throws RemoteException
     */
    private SwitchResult createSwitch()
    throws RemoteException {
        String newSwitchName = UUID.randomUUID().toString().replaceAll("-", "");
        if (newSwitchName.length() > 10) {
            newSwitchName = newSwitchName.substring(0, 10);
        }

        SwitchResult result;
        log.debug("Host path: " + hostPath);
        log.debug("Host: " + host);
        ComputeResource res = host.getComputeResource(hostPath);
        HostSystem hostSys = res.getHosts()[0];
        HostNetworkSystem networkSystem = hostSys.getHostNetworkSystem();

        HostVirtualSwitchSpec switchSpec = new HostVirtualSwitchSpec();
        switchSpec.setNumPorts(portCount);
        networkSystem.addVirtualSwitch(newSwitchName, switchSpec);

        String newPortGroupName = newSwitchName + "-network";
        HostPortGroupSpec portGroupSpec = new HostPortGroupSpec();
        portGroupSpec.setName(newPortGroupName);
        portGroupSpec.setVswitchName(newSwitchName);
        portGroupSpec.setVlanId(vlanId);
        portGroupSpec.setPolicy(new HostNetworkPolicy());
        networkSystem.addPortGroup(portGroupSpec);

        Network network = null;
        for (Network n : hostSys.getNetworks()) {
            if (n.getName().equals(newPortGroupName)) {
                network = n;
                break;
            }
        }
        if (network == null) {
            throw new RemoteException("Network created but not found");
        }

        Path switchPath = new Path(hostPath, newSwitchName);
        Path portGroupPath = new Path(hostPath, newPortGroupName);
        result = new SwitchResult(network, switchPath, portGroupPath);
        this.switchName = newSwitchName;
        this.portGroupName = newPortGroupName;

        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Restores the SwitchResult object for the network task
     * @param host
     * @throws RemoteException
     */
    public void restoreNetwork(VirtualHost host)
    throws RemoteException {
        ComputeResource res = host.getComputeResource(hostPath);
        HostSystem hostSys = res.getHosts()[0];

        Network network = null;
        for (Network n : hostSys.getNetworks()) {
            if (n.getName().equals(portGroupName)) {
                network = n;
                break;
            }
        }
        if (network == null) {
            throw new RemoteException("Could not restore network from xml.");
        }

        Path switchPath = new Path(hostPath, switchName);
        Path portGroupPath = new Path(hostPath, portGroupName);
        SwitchResult switchResult = new SwitchResult(network, switchPath, portGroupPath);
        this.vSwitch = switchResult;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        try {
            this.vSwitch = createSwitch();
        }
        catch (RemoteException e) {
            log.warn("RemoteException while creating virtual switch", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        try {
            restoreNetwork(host);
            host.removePortGroup(vSwitch.getPortGroupPath());
            host.removeSwitch(vSwitch.getSwitchPath());
        }
        catch (RemoteException e) {
            log.warn("Unable to delete network", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {
        try {
            restoreNetwork(host);
        }
        catch (RemoteException e) {
            log.warn("Unable to restore network", e);
        }
    }
}

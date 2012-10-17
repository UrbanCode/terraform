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
package org.urbancode.terraform.tasks.vmware.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineToolsStatus;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostNetworkSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.SearchIndex;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VirtualHost implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    /**
     * This class contains utility methods for handling VSphere objects.
     * It also stores the vSphere ServiceInstance object for connecting to vCenter.
     * There is a nonzero chance that some of these methods will be refactored in to
     * CloneTask, NetworkTask, NetworkRefTask, and other classes.
     */
    final static private Logger log = Logger.getLogger(VirtualHost.class);
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ServiceInstance serviceInstance;
    private String url;
    private String user;
    private String password;

    //----------------------------------------------------------------------------------------------
    public VirtualHost(String url, String user, String password)
    throws RemoteException, MalformedURLException {
        this.url = url;
        this.user = user;
        this.password = password;
        serviceInstance = new ServiceInstance(new URL(url), user, password, true);
    }

    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return this.url;
    }

    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return this.user;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return this.password;
    }

    //----------------------------------------------------------------------------------------------
    public ServiceInstance getServiceInstance() {
        return this.serviceInstance;
    }

    //----------------------------------------------------------------------------------------------
    public void close() {
        serviceInstance.getServerConnection().logout();
    }

    //----------------------------------------------------------------------------------------------
    public VirtualEthernetCard getNic(VirtualMachine vm, int nicIndex)
    throws RemoteException {
        if (nicIndex < 0) {
            throw new IllegalArgumentException("Invalid nic index");
        }

        List<VirtualEthernetCard> nics = new ArrayList<VirtualEthernetCard>();
        VirtualDevice[] devices = vm.getConfig().getHardware().getDevice();
        for (VirtualDevice device : devices) {
            if (device instanceof VirtualEthernetCard) {
                nics.add((VirtualEthernetCard) device);
            }
        }

        if (nics.size() < nicIndex) {
            throw new IllegalArgumentException("Invalid nic index");
        }

        return nics.get(nicIndex);
    }

    //----------------------------------------------------------------------------------------------
    public List<VirtualEthernetCard> getAllNics(VirtualMachine vm)
    throws RemoteException {

        List<VirtualEthernetCard> nics = new ArrayList<VirtualEthernetCard>();
        VirtualDevice[] devices = vm.getConfig().getHardware().getDevice();
        for (VirtualDevice device : devices) {
            if (device instanceof VirtualEthernetCard) {
                nics.add((VirtualEthernetCard) device);
            }
        }

        return nics;
    }

    //----------------------------------------------------------------------------------------------
    public void attachNic(VirtualMachine vm, VirtualEthernetCard nic, Network network)
    throws RemoteException, InterruptedException {
        VirtualEthernetCardNetworkBackingInfo backing =
            (VirtualEthernetCardNetworkBackingInfo) nic.getBacking();
        backing.setDeviceName(network.getName());
        backing.setNetwork(network.getMOR());

        nic.setBacking(backing);
        nic.getDeviceInfo().setSummary(network.getName());

        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
        nicSpec.setDevice(nic);

        VirtualDeviceConfigSpec[] configSpecs = new VirtualDeviceConfigSpec[1];
        configSpecs[0] = nicSpec;

        VirtualMachineConfigSpec machineConfigSpec = new VirtualMachineConfigSpec();
        machineConfigSpec.setDeviceChange(configSpecs);

        Task task = vm.reconfigVM_Task(machineConfigSpec);
        String status = task.waitForTask();
        if (!status.equals("success")) {
            String message = task.getTaskInfo().getError().getLocalizedMessage();
            throw new RemoteException("Failed: " + message);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void removeSwitch(Path path)
    throws RemoteException {
        ComputeResource res = getComputeResource(path.getParent());
        HostSystem host = res.getHosts()[0];
        HostNetworkSystem networkSystem = host.getHostNetworkSystem();

        try {
            networkSystem.removeVirtualSwitch(path.getName());
        }
        catch (NotFound swallow) {
        }
    }

    //----------------------------------------------------------------------------------------------
    public void removePortGroup(Path path)
    throws RemoteException {
        ComputeResource res = getComputeResource(path.getParent());
        HostSystem host = res.getHosts()[0];
        HostNetworkSystem networkSystem = host.getHostNetworkSystem();
        try {
            networkSystem.removePortGroup(path.getName());
        }
        catch (NotFound swallow) {
        }
    }

    //----------------------------------------------------------------------------------------------
    public VirtualMachine getVm(UUID vmid)
    throws RemoteException {
        SearchIndex searchIndex = serviceInstance.getSearchIndex();
        VirtualMachine vm = (VirtualMachine) searchIndex.findByUuid(
            null, vmid.toString(), true, true);
        if (vm == null) {
            throw new NotFound();
        }
        return vm;
    }

    //----------------------------------------------------------------------------------------------
    public String getVmxPath(VirtualMachine vm) {
        return vm.getConfig().getFiles().getVmPathName();
    }

    //----------------------------------------------------------------------------------------------
    public void waitForIp(VirtualMachine vm)
    throws RemoteException, InterruptedException {
        long pollInterval = 3000L;
        long timeoutInterval = 10L * 60L * 1000L;
        long start = System.currentTimeMillis();

        String address = vm.getGuest().getIpAddress();
        while (address == null) {
            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException("Timeout waiting for IP");
            }
            Thread.sleep(pollInterval);
            address = vm.getGuest().getIpAddress();
        }
        assert address != null;
    }

    //----------------------------------------------------------------------------------------------
    public void waitForIp(UUID vmid)
    throws RemoteException, InterruptedException {
        waitForIp(getVm(vmid));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * This method will wait for VM Tools to be running. For example, the machine might
     * still be booting and thus VM Tools would not be running.
     * @param vm
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void waitForVmtools(VirtualMachine vm)
    throws RemoteException, InterruptedException {
        long pollInterval = 3000L;
        long timeoutInterval = 5L * 60L * 1000L;
        long start = System.currentTimeMillis();

        VirtualMachineToolsStatus toolsStatus = vm.getGuest().getToolsStatus();
        while (toolsStatus != VirtualMachineToolsStatus.toolsOk &&
               toolsStatus != VirtualMachineToolsStatus.toolsOld)
        {
            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException("Timeout waiting for vmtools");
            }
            Thread.sleep(pollInterval);
            toolsStatus = vm.getGuest().getToolsStatus();
        }
    }

    //----------------------------------------------------------------------------------------------
    public void waitForVmtools(UUID vmid)
    throws RemoteException, InterruptedException {
        waitForVmtools(getVm(vmid));
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param path
     * @return the ComputerResource associated with the datacenter and host.
     * @throws RemoteException
     */
    public ComputeResource getComputeResource(Path path)
    throws RemoteException {
        ComputeResource result = null;

        String hostName = path.getHostName();
        Datacenter datacenter = getDatacenter(path);

        for (ManagedEntity e : datacenter.getHostFolder().getChildEntity()) {
            if (e instanceof ComputeResource && e.getName().equals(hostName)) {
                result = (ComputeResource) e;
                break;
            }
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Datacenter getDatacenter(Path path)
    throws RemoteException {
        Datacenter result = null;
        String name = path.getDatacenterName();
        Folder root = serviceInstance.getRootFolder();
        for (ManagedEntity e : root.getChildEntity()) {
            if (e instanceof Datacenter && e.getName().equals(name)) {
                result = (Datacenter) e;
                break;
            }
        }
        if (result == null) {
            log.warn("could not find datacenter on path " + path.toString());
            throw new NotFound();
        }
        return result;
    }

}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.util.IOUtil;

import com.vmware.vim25.HostNetworkPolicy;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineToolsStatus;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
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
    public VirtualHost(VmHost host)
    throws RemoteException, MalformedURLException {
        this(host.getUrl(), host.getUser(), host.getPassword());
    }

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
    public void powerOnVm(VirtualMachine vm)
    throws RemoteException, InterruptedException {
        Task task = vm.powerOnVM_Task(null);
        String status = task.waitForTask();
        if (!status.equals("success")) {
            VirtualMachinePowerState powerState = vm.getRuntime().getPowerState();
            if (powerState != VirtualMachinePowerState.poweredOn) {
                String message = task.getTaskInfo().getError().getLocalizedMessage();
                throw new RemoteException("Failed: " + message);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void powerOnVm(UUID vmid)
    throws RemoteException, InterruptedException {
        powerOnVm(getVm(vmid));
    }

    //----------------------------------------------------------------------------------------------
    public void powerOffVm(VirtualMachine vm)
    throws RemoteException, InterruptedException {
        Task task = vm.powerOffVM_Task();
        String status = task.waitForTask();
        if (!status.equals("success")) {
            VirtualMachinePowerState powerState = vm.getRuntime().getPowerState();
            if (powerState != VirtualMachinePowerState.poweredOff) {
                String message = task.getTaskInfo().getError().getLocalizedMessage();
                throw new RemoteException("Failed: " + message);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void powerOffVm(UUID vmid)
    throws RemoteException, InterruptedException {
        powerOffVm(getVm(vmid));
    }

    //----------------------------------------------------------------------------------------------
    public SwitchResult createSwitch(Path hostPath, String name, int portCount)
    throws RemoteException, NamingException {
        log.debug("Name used when Creating Virtual Switch: " + name);
        if (name.length() > 31) {
            log.error("WARNING: " +
                      "Creation of the Virtual Switch will fail if the name is > 31 characters!");
            throw new NamingException("Bad Virtual Switch Name");
        }

        SwitchResult result;
        ComputeResource res = getComputeResource(hostPath);
        HostSystem host = res.getHosts()[0];
        HostNetworkSystem networkSystem = host.getHostNetworkSystem();

        HostVirtualSwitchSpec switchSpec = new HostVirtualSwitchSpec();
        switchSpec.setNumPorts(portCount);
        networkSystem.addVirtualSwitch(name, switchSpec);

        String portGroupName = name + "-network";
        HostPortGroupSpec portGroupSpec = new HostPortGroupSpec();
        portGroupSpec.setName(portGroupName);
        portGroupSpec.setVswitchName(name);
        portGroupSpec.setPolicy(new HostNetworkPolicy());
        networkSystem.addPortGroup(portGroupSpec);

        Network network = null;
        for (Network n : host.getNetworks()) {
            if (n.getName().equals(portGroupName)) {
                network = n;
                break;
            }
        }
        if (network == null) {
            throw new RemoteException("Network created but not found");
        }

        result = new SwitchResult(
            network,
            new Path(hostPath, name),
            new Path(hostPath, portGroupName));

        return result;
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
    public void removeVm(UUID vmid)
    throws RemoteException, InterruptedException {
        try {
            VirtualMachine vm = getVm(vmid);
            Task task = vm.destroy_Task();
            String status = task.waitForTask();
            if (!status.equals("success")) {
                // throw NotFound if vm no longer exists
                getVm(vmid);

                String message = task.getTaskInfo().getError().getLocalizedMessage();
                throw new RemoteException("Failed: " + message);
            }
        }
        catch (NotFound swallow) {
        }
    }

    //----------------------------------------------------------------------------------------------
    public Network getNetwork(Path path)
    throws RemoteException {
        Network result = null;

        String networkName = path.getName();

        ComputeResource res = getComputeResource(path);
        HostSystem host = res.getHosts()[0];
        for (Network network : host.getNetworks()) {
            if (network.getName().equals(networkName)) {
                result = network;
                break;
            }
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
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
    public void runCommand(VirtualMachine vm, String user, String password, String... command)
    throws IOException, InterruptedException {
        runCommand(vm, user, password, Arrays.asList(command));
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(VirtualMachine vm, String user, String password, List<String> command)
    throws IOException, InterruptedException {
        waitForVmtools(vm);
        String vmx = getVmxPath(vm);
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("vmrun");
        commandLine.add("-T");
        commandLine.add("server");
        commandLine.add("-h");
        commandLine.add(url);
        commandLine.add("-u");
        commandLine.add(this.user);
        commandLine.add("-p");
        commandLine.add(this.password);
        commandLine.add("-gu");
        commandLine.add(user);
        commandLine.add("-gp");
        commandLine.add(password);
        commandLine.add("runProgramInGuest");
        commandLine.add(vmx);
        commandLine.addAll(command);
        if (log.isDebugEnabled()) {
            log.debug("Command: " + commandLine);
        }
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        
        InputStream procIn = process.getInputStream();
        IOUtil.getInstance().discardStream(procIn);
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with code " + exitCode);
        }
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
    public VirtualMachine getTemplate(Path path)
    throws RemoteException {
        VirtualMachine result = null;

        List<String> folderNames = path.getParentFolders().toList();
        String targetName = path.getName();

        Datacenter datacenter = getDatacenter(path);

        // traverse folders
        Folder folder = datacenter.getVmFolder();
        Folder nextFolder = null;
        for (String folderName : folderNames) {
            for (ManagedEntity e : folder.getChildEntity()) {
                if (e instanceof Folder && e.getName().equals(folderName)) {
                    nextFolder = (Folder) e;
                    break;
                }
            }
            if (nextFolder == null) {
                throw new NotFound();
            }
            folder = nextFolder;
            nextFolder = null;
        }
        if (folder == null) {
            throw new NotFound();
        }

        // find template
        for (ManagedEntity e : folder.getChildEntity()) {
            if (e instanceof VirtualMachine && e.getName().equals(targetName)) {
                VirtualMachine vm = (VirtualMachine) e;
                if (vm.getConfig().isTemplate()) {
                    result = vm;
                    break;
                }
            }
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Datastore getDatastore(Path path)
    throws RemoteException {
        Datastore result = null;

        Datacenter datacenter = getDatacenter(path);

        for (Datastore d : datacenter.getDatastores()) {
            if (d.getName().equals(path.getName())) {
                result = d;
                break;
            }
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
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
            log.debug("could not find datacenter on path " + path.toString());
            throw new NotFound();
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    Folder getFolder(Path path)
    throws RemoteException {
        Folder result = null;

        List<String> folderNames = path.getFolders().toList();

        Datacenter datacenter = getDatacenter(path);

        // traverse folders
        result = datacenter.getVmFolder();
        Folder nextFolder = null;
        for (String folderName : folderNames) {
            for (ManagedEntity e : result.getChildEntity()) {
                if (e instanceof Folder && e.getName().equals(folderName)) {
                    nextFolder = (Folder) e;
                    break;
                }
            }
            if (nextFolder == null) {
                throw new NotFound();
            }
            result = nextFolder;
            nextFolder = null;
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }
}

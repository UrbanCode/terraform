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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.vmware.events.CloneVmCreatedEvent;
import org.urbancode.terraform.tasks.vmware.events.TaskEventListener;
import org.urbancode.terraform.tasks.vmware.events.TaskEventService;
import org.urbancode.terraform.tasks.vmware.util.GlobalIpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.Ip4;
import org.urbancode.terraform.tasks.vmware.util.IpInUseException;
import org.urbancode.terraform.tasks.vmware.util.Path;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateDiskMoveOptions;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineToolsStatus;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class CloneTask extends SubTask implements Cloneable, Comparable<CloneTask> {

    // **********************************************************************************************
    // CLASS
    // **********************************************************************************************
    static private final Logger log = Logger.getLogger(CloneTask.class);

    // **********************************************************************************************
    // INSTANCE
    // **********************************************************************************************
    private String instanceName;
    private String snapshotName = "";

    private int serverCount = 1;
    private int order = 1;  // default to front of queue. max int is just ugly

    private VirtualMachine vm;
    private Path imagePath;

    private boolean poweredOn = false;
    private boolean assignHostIp = false;
    private boolean sentPowerDown = false;

    private EnvironmentTaskVmware environment;

    private List<Ip4> ipList = new ArrayList<Ip4>();
    private List<NetworkRefTask> networkRefs = new ArrayList<NetworkRefTask>();
    private List<SecurityGroupRefTask> securityGroupRefs = new ArrayList<SecurityGroupRefTask>();
    private List<TaskEventListener> listeners = new ArrayList<TaskEventListener>();
    private List<PostCreateTask> postCreateTaskList = new ArrayList<PostCreateTask>();

    private String user;
    private String password;

    //----------------------------------------------------------------------------------------------
    public CloneTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public CloneTask(EnvironmentTaskVmware environment) {
        super();
        this.environment = environment;
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskVmware fetchEnvironment() {
        return this.environment;
    }

    //----------------------------------------------------------------------------------------------
    public VirtualMachine fetchVm() {
        return this.vm;
    }

    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return password;
    }

    //----------------------------------------------------------------------------------------------
    public Path getImagePath() {
        return this.imagePath;
    }

    //----------------------------------------------------------------------------------------------
    public String getSnapshotName() {
        return this.snapshotName;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getPoweredOn() {
        return this.poweredOn;
    }

    //----------------------------------------------------------------------------------------------
    public String getInstanceName() {
        return this.instanceName;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getAssignHostIp() {
        return this.assignHostIp;
    }

    //----------------------------------------------------------------------------------------------
    public int getOrder() {
        return this.order;
    }

    //----------------------------------------------------------------------------------------------
    public String getIpList() {
        String result;
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Ip4 ip : ipList) {
            if (first) {
                builder.append(ip.toString());
                first = false;
            }
            else {
                builder.append("," + ip.toString());
            }
        }
        result = builder.toString();
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public List<NetworkRefTask> getNetworkRefs() {
        return this.networkRefs;
    }

    //----------------------------------------------------------------------------------------------
    public List<SecurityGroupRefTask> getSecurityGroupRefs() {
        return this.securityGroupRefs;
    }

    //----------------------------------------------------------------------------------------------
    public List<PostCreateTask> getPostCreateTaskList() {
        return this.postCreateTaskList;
    }

    //----------------------------------------------------------------------------------------------
    public List<TaskEventListener> getEventListeners() {
        return this.listeners;
    }

    //----------------------------------------------------------------------------------------------
    public void setUser(String user) {
        this.user = user;
    }

    //----------------------------------------------------------------------------------------------
    public void setPassword(String password) {
        this.password = password;
    }

    //----------------------------------------------------------------------------------------------
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    //----------------------------------------------------------------------------------------------
    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    //----------------------------------------------------------------------------------------------
    public void setImagePath(String imagePathString) {
        Path imagePth = new Path(imagePathString);
        this.imagePath = imagePth;
    }

    //----------------------------------------------------------------------------------------------
    public void setPoweredOn(boolean poweredOn) {
        this.poweredOn = poweredOn;
    }

    //----------------------------------------------------------------------------------------------
    public void setAssignHostIp(boolean assignHostIp) {
        this.assignHostIp = assignHostIp;
    }

    //----------------------------------------------------------------------------------------------
    public void setOrder(int order) {
        this.order = order;
    }

    //----------------------------------------------------------------------------------------------
    public void setIpList(String ipListAsString) {
        ipList.clear();
        String[] split = ipListAsString.split(",");
        for (int i=0; i<split.length; i++) {
            Ip4 ip = new Ip4(split[i]);
            ipList.add(ip);
            //ensure IPs are reserved
            GlobalIpAddressPool globalIpPool = GlobalIpAddressPool.getInstance();
            try {
                globalIpPool.reserveIp(ip);
            }
            catch(IpInUseException e) {
                log.error("Ip " + ip + " already in use!");
                // TODO - throw
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setIpListFromVmInfo() {
        ipList.clear();
        GuestNicInfo[] nicInfos = vm.getGuest().getNet();
        if (nicInfos == null) {
            log.warn("problem retrieving network info from VM");
        }
        for (int i=0; i<nicInfos.length; i++) {
            String[] nicInfoIpList = nicInfos[i].getIpAddress();
            for (int j=0; j<nicInfoIpList.length; j++) {
                //ip4 addresses only; vsphere 5 likes to return ip4 and ip6
                String unparsedIp = nicInfoIpList[j];
                if (!unparsedIp.contains(":")) {
                    Ip4 newIp = new Ip4(unparsedIp);
                    ipList.add(newIp);
                }

            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    //----------------------------------------------------------------------------------------------
    public void setEnvironment(EnvironmentTaskVmware environment) {
        this.environment = environment;
    }

    //----------------------------------------------------------------------------------------------
    public TaskEventListener addEventListener(TaskEventListener listener) {
        this.listeners.add(listener);
        listener.setValues(this);
        return listener;
    }

    //----------------------------------------------------------------------------------------------
    public PostCreateTask addPostCreateTask(PostCreateTask task) {
        task.setValues(this);
        this.postCreateTaskList.add(task);
        return task;
    }

    //----------------------------------------------------------------------------------------------
    public PostCreateTask addCommand(PostCreateTask task) {
        task.setValues(this);
        this.postCreateTaskList.add(task);
        return task;
    }

    //----------------------------------------------------------------------------------------------
    public NetworkRefTask createNetworkRef() {
        NetworkRefTask result = new NetworkRefTask();
        this.networkRefs.add(result);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public SecurityGroupRefTask createSecurityGroupRef() {
        SecurityGroupRefTask result = new SecurityGroupRefTask();
        this.securityGroupRefs.add(result);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        log.debug("Calling create method on clone");

        // update server name property
        //String serverProp = environment.getName() + "-" + instanceName;
        //context.setProperty("ud.agent.name", serverProp);

        TaskEventService eventService = environment.fetchEventService();
        for (TaskEventListener listener : listeners) {
            eventService.addEventListener(listener);
        }

        try {
            this.vm = cloneVM();

            for (NetworkRefTask networkRef : networkRefs) {
                networkRef.setVirtualSwitch(environment.restoreNetworkForName(networkRef.getNetworkName()).fetchSwitch());
                networkRef.setVirtualMachine(this.vm);
                networkRef.setVirtualHost(environment.fetchVirtualHost());
                networkRef.create();
            }

            for (SecurityGroupRefTask sgr : securityGroupRefs) {
                sgr.setSecurityGroup(environment.restoreSecurityGroupForName(sgr.getName()));
            }
        }
        catch (RemoteException e) {
            log.warn("remote exception when creating clone task", e);
        }
        catch (InterruptedException e) {
            log.warn("interrupted exception when creating clone task", e);
        }
        catch (Exception e) {
            log.warn("unknown exception when creating clone task", e);
        }

        CloneVmCreatedEvent actionEvent = new CloneVmCreatedEvent(this);
        environment.fetchEventService().sendEvent(actionEvent);

        runPostCreateTasks();

        try {
            powerOnVm();
        }
        catch (Exception e) {
            log.warn("exception when powering on vm", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        try {
            restoreVm();
            GlobalIpAddressPool globalIpPool = GlobalIpAddressPool.getInstance();
            for (Ip4 ip : ipList) {
                globalIpPool.releaseIp(ip);
            }
            powerOffVm();
            removeVm();
            for(PostCreateTask pct : postCreateTaskList) {
                pct.destroy();
            }
        }
        catch (RemoteException e) {
            log.warn("remote exception when deleting clone task", e);
        }
        catch (InterruptedException e) {
            log.warn("interruption exception when deleting clone task", e);
        }

    }

    //----------------------------------------------------------------------------------------------
    public void runPostCreateTasks() {
        for (PostCreateTask task : postCreateTaskList) {
            task.setUser(user);
            task.setPassword(password);
            task.create();
        }
    }

    //----------------------------------------------------------------------------------------------
    public VirtualMachine cloneVM()
    throws RemoteException, InterruptedException {
        VirtualMachine result = null;

        VirtualHost host = environment.fetchVirtualHost();
        Path hostPath = environment.getHostPath();
        Path storePath = environment.getDatastorePath();
        Folder destFolder = environment.fetchFolderTask().getFolder();

        boolean isTemplate = !(snapshotName.length() > 0);
        VirtualMachine template = null;
        template = fetchOriginalVm(imagePath, isTemplate);

        ComputeResource computeResource = host.getComputeResource(hostPath);
        ResourcePool pool = computeResource.getResourcePool();

        VirtualMachineRelocateSpec location = new VirtualMachineRelocateSpec();
        location.setPool(pool.getMOR());
        if (storePath != null) {
            location.setDatastore(fetchDatastore(storePath).getMOR());
        }

        VirtualMachineCloneSpec spec = new VirtualMachineCloneSpec();
        spec.setLocation(location);
        spec.setPowerOn(poweredOn);

        if (!isTemplate) {
            log.info("creating vm " + instanceName + " from snapshot "
                    + snapshotName);
            ManagedObjectReference snapshotMOR = fetchSnapshotRef(template,
                    snapshotName);
            spec.setSnapshot(snapshotMOR);
            location.setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.createNewChildDiskBacking
                    .toString());
        }

        Task task = template.cloneVM_Task(destFolder, instanceName, spec);
        @SuppressWarnings("unused")
        String status = task.waitForTask();

        Object taskResult = task.getTaskInfo().getResult();
        if (taskResult instanceof VirtualMachine) {
            result = (VirtualMachine) taskResult;
        } else if (taskResult instanceof ManagedObjectReference) {
            result = new VirtualMachine(host.getServiceInstance()
                    .getServerConnection(), (ManagedObjectReference) taskResult);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private ManagedObjectReference traverseSnapshotInTree(
            VirtualMachineSnapshotTree[] snapTree, String snapshotName) {
        ManagedObjectReference result = null;

        if (snapTree != null && snapshotName != null) {
            for (int i = 0; i < snapTree.length && result == null; i++) {
                VirtualMachineSnapshotTree node = snapTree[i];
                if (node.getName().equals(snapshotName)) {
                    result = node.getSnapshot();
                } else {
                    VirtualMachineSnapshotTree[] childTree = snapTree[i]
                            .getChildSnapshotList();
                    result = traverseSnapshotInTree(childTree, snapshotName);
                }
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public int fetchServerCount() {
        return this.serverCount;
    }

    //----------------------------------------------------------------------------------------------
    private ManagedObjectReference fetchSnapshotRef(VirtualMachine vm,
            String snapshotName) {
        ManagedObjectReference result = null;
        VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();

        if (snapInfo != null) {
            VirtualMachineSnapshotTree[] snapTree = snapInfo
                    .getRootSnapshotList();
            result = traverseSnapshotInTree(snapTree, snapshotName);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Datastore fetchDatastore(Path path)
    throws RemoteException {
        Datastore result = null;

        VirtualHost host = environment.fetchVirtualHost();
        Datacenter datacenter = host.getDatacenter(path);

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
    public VirtualMachine fetchOriginalVm(Path path, boolean isTemplate)
            throws RemoteException {
        VirtualMachine result = null;

        Folder folder = fetchFolder(path.getParent());
        String targetName = path.getName();

        // find template
        for (ManagedEntity e : folder.getChildEntity()) {
            if (e instanceof VirtualMachine && e.getName().equals(targetName)) {
                VirtualMachine vmTemplate = (VirtualMachine) e;
                if (vmTemplate.getConfig().isTemplate() == isTemplate) {
                    result = vmTemplate;
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
    public VirtualMachine fetchVmForPath(Path path)
    throws RemoteException {
        VirtualMachine result = null;

        Folder folder = fetchFolder(path.getParent());
        String targetName = path.getName();
        log.debug("looking for vm " + targetName + " in folder " + path.getParent().toString());

        // find template
        for (ManagedEntity e : folder.getChildEntity()) {
            if (e instanceof VirtualMachine && e.getName().equals(targetName)) {
                VirtualMachine vmTemplate = (VirtualMachine) e;
                result = vmTemplate;
                break;
            }
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Folder fetchFolder(Path path)
    throws RemoteException {
        Folder result = null;

        VirtualHost host = environment.fetchVirtualHost();
        List<String> folderNames = path.getFolders().toList();
        Datacenter datacenter = host.getDatacenter(path);

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

    //----------------------------------------------------------------------------------------------
    public String fetchVmStatus() {
        String result = null;
        try {
            if (vm == null) {
                if (sentPowerDown) {
                    result = "Shut Down";
                }
                else {
                    result = "Starting";
                }
            }
            else {
                GuestInfo guestInfo = vm.getGuest();
                VirtualMachineToolsStatus vmToolsStatus = guestInfo.getToolsStatus();
                VirtualMachineRuntimeInfo vmri = vm.getRuntime();
                VirtualMachinePowerState powerStatus = vmri.getPowerState();

                if (vmToolsStatus.equals(VirtualMachineToolsStatus.toolsOk) && powerStatus.equals(VirtualMachinePowerState.poweredOn)) {
                    if (sentPowerDown) {
                        result = "Shutting Down";
                    }
                    else {
                        result = "Running";
                    }
                }
                else if (vmToolsStatus.equals(VirtualMachineToolsStatus.toolsOld) || vmToolsStatus.equals(VirtualMachineToolsStatus.toolsNotInstalled)) {
                    if (powerStatus.equals(VirtualMachinePowerState.poweredOn)) {
                        if (sentPowerDown) {
                            result = "Shutting Down";
                        }
                        else {
                            result = "Powered On";
                        }
                    }
                    else if (powerStatus.equals(VirtualMachinePowerState.poweredOff)) {
                        if (sentPowerDown) {
                            result = "Shutting Down";
                        }
                        else {
                            result = "Powered Off Or Starting";
                        }
                    }
                }
                else if (vmToolsStatus.equals(VirtualMachineToolsStatus.toolsNotRunning)) {
                    if (powerStatus.equals(VirtualMachinePowerState.poweredOn)) {
                        result = "Starting";
                    }
                    else if (powerStatus.equals(VirtualMachinePowerState.poweredOff)) {
                        result = "Not Started or Powered Off";
                    }
                }
            }
        }
        catch(Exception e) {
            if (sentPowerDown) {
                result = "Shut Down";
            }
            else {
                result = "Starting";
            }
        }
        if (result == null) {
            result = "Unknown";
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void powerOffVm()
    throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {
        VirtualMachineRuntimeInfo vmri = vm.getRuntime();
        if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn) {
            sentPowerDown = true;
            com.vmware.vim25.mo.Task task = vm.powerOffVM_Task();
            task.waitForTask();
            log.info("vm:" + vm.getName() + " powered off.");
        }
    }

    //----------------------------------------------------------------------------------------------
    public void powerOnVm()
    throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {
        Path hostPath = this.environment.getHostPath();
        VirtualHost host = this.environment.fetchVirtualHost();
        ComputeResource res = host.getComputeResource(hostPath);
        HostSystem hostSystem = res.getHosts()[0];
        VirtualMachineRuntimeInfo vmri = vm.getRuntime();
        if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff) {
            com.vmware.vim25.mo.Task task = vm.powerOnVM_Task(hostSystem);
            task.waitForTask();
            log.info("vm:" + vm.getName() + " powered on.");
        }
    }

    //----------------------------------------------------------------------------------------------
    public void restoreVm()
    throws RemoteException {
        // restore by vm name and folder
        Path folderPath = environment.fetchFolderTask().getFolderRef();
        Path vmPath = new Path(folderPath, instanceName);
        this.vm = fetchOriginalVm(vmPath, false);
    }

    //----------------------------------------------------------------------------------------------
    public void removeVm()
    throws RemoteException, InterruptedException {
        try {
            Task task = vm.destroy_Task();
            String status = task.waitForTask();
            if (!status.equals("success")) {
                String message = task.getTaskInfo().getError()
                        .getLocalizedMessage();
                throw new RemoteException("Failed: " + message);
            }
        } catch (NotFound e) {
            log.warn("VM not found before attempting to delete it", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int compareTo(CloneTask o) {
        int result;
        int other = o.getOrder();
        //if order is not specified, default is max_int (back of the queue)
        //lowest order number is highest priority
        if (this.order < other) {
            result = -1;
        }
        else if (this.order == other) {
            result = 0;
        }
        else {
            result = 1;
        }
        return result;
    }

}

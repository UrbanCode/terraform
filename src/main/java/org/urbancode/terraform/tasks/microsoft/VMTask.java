package org.urbancode.terraform.tasks.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

import com.urbancode.x2o.tasks.SubTask;

public class VMTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VMTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String vmName;
    private String roleFileName = "";
    private boolean addUuid = false;
    private String uuid = "";
    private String imageName;
    private String location;
    private String affinityGroup;
    private String size = "small";
    private String user;
    private String password = "";
    private List<EndpointTask> endpointTasks = new ArrayList<EndpointTask>();
    private boolean ssh = false;
    private boolean rdp = false;
    private String virtualNetworkName = "";
    private String subnetNames = "";
    private String blobUrl = "";

    //----------------------------------------------------------------------------------------------
    public VMTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getVmName() {
        return vmName;
    }

    //----------------------------------------------------------------------------------------------
    public String getImageName() {
        return imageName;
    }

    //----------------------------------------------------------------------------------------------
    public String getLocation() {
        return location;
    }

    //----------------------------------------------------------------------------------------------
    public String getAffinityGroup() {
        return affinityGroup;
    }

    //----------------------------------------------------------------------------------------------
    public String getSize() {
        return size;
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
    public String getRoleFileName() {
        return roleFileName;
    }

    //----------------------------------------------------------------------------------------------
    public String getVirtualNetworkName() {
        return virtualNetworkName;
    }

    //----------------------------------------------------------------------------------------------
    public String getSubnetNames() {
        return subnetNames;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getSsh() {
        return ssh;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getRdp() {
        return rdp;
    }

    //----------------------------------------------------------------------------------------------
    public String getBlobUrl() {
        return blobUrl;
    }

    //----------------------------------------------------------------------------------------------
    public List<EndpointTask> getEndpointTasks() {
        return endpointTasks;
    }

    //----------------------------------------------------------------------------------------------
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    //----------------------------------------------------------------------------------------------
    public void setAddUuid(boolean addUuid) {
        this.addUuid = addUuid;
    }

    //----------------------------------------------------------------------------------------------
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    //----------------------------------------------------------------------------------------------
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    //----------------------------------------------------------------------------------------------
    public void setLocation(String location) {
        this.location = location;
    }

    //----------------------------------------------------------------------------------------------
    public void setAffinityGroup(String affinityGroup) {
        this.affinityGroup = affinityGroup;
    }

    //----------------------------------------------------------------------------------------------
    public void setSize(String size) {
        this.size = size;
    }

    //----------------------------------------------------------------------------------------------
    public void setVmSize(String size) {
        //alias for size
        this.size = size;
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
    public void setRoleFileName(String roleFileName) {
        this.roleFileName = roleFileName;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualNetworkName(String virtualNetworkName) {
        this.virtualNetworkName = virtualNetworkName;
    }

    //----------------------------------------------------------------------------------------------
    public void setSubnetNames(String subnetNames) {
        this.subnetNames = subnetNames;
    }

    //----------------------------------------------------------------------------------------------
    public void setSsh(boolean ssh) {
        this.ssh = ssh;
    }

    //----------------------------------------------------------------------------------------------
    public void setRdp(boolean rdp) {
        this.rdp = rdp;
    }

    //----------------------------------------------------------------------------------------------
    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    //----------------------------------------------------------------------------------------------
    public EndpointTask createEndpointTask() {
        EndpointTask task = new EndpointTask();
        task.setDnsName(vmName);
        endpointTasks.add(task);
        return task;
    }

    //----------------------------------------------------------------------------------------------
    private boolean isValidString(String s) {
        return !(s == null || "".equals(s) || "null".equals(s));
    }

    //----------------------------------------------------------------------------------------------
    public List<String> makeCommandList() {
        List<String> result = new ArrayList<String>();
        result.add("vm");
        if(isValidString(roleFileName)) {
            result.add("create-from");
            result.add(vmName);
            result.add(roleFileName);
        }
        else {
            result.add("create");
            result.add(vmName);
            result.add(imageName);
            result.add(user);
            result.add(password);
            result.add("--vm-size");
            result.add(size);
            if(isValidString(subnetNames)) {
                result.add("--subnet-names");
                result.add(subnetNames);
            }
            if (ssh) {
                result.add("--ssh");
            }
            if (rdp) {
                result.add("--rdp");
            }
        }

        if (isValidString(affinityGroup)) {
            result.add("--affinity-group");
            result.add(affinityGroup);
        }
        else if (isValidString(location)) {
            result.add("--location");
            result.add(location);
        }
        else {
            throw new IllegalArgumentException("no affinity group or location was specified. " +
            		"Please specify one or the other in your template.");
        }


        if(isValidString(virtualNetworkName)) {
            result.add("--virtual-network-name");
            result.add(virtualNetworkName);
        }

        if(isValidString(blobUrl)) {
            result.add("--blob-url");
            result.add(blobUrl);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        vmName = addUuid ? vmName + "-" + uuid : vmName;
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand(makeCommandList());

        for(EndpointTask task : endpointTasks) {
            task.create();
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        AzureCmdRunner runner = new AzureCmdRunner();
        runner.runCommand("vm", "delete", vmName);
    }

}

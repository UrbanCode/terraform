package org.urbancode.terraform.tasks.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.microsoft.util.AzureCmdRunner;

public class VMTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VMTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String vmName;
    private boolean addUuid = false;
    private String uuid;
    private String imageName;
    private String location;
    private String size = "small";
    private String user;
    private String password = "";
    private String roleFileName = "";
    private List<EndpointTask> endpointTasks = new ArrayList<EndpointTask>();
    private boolean ssh = false;
    private boolean rdp = false;
    private String virtualNetworkName = "";
    private String subnetNames = "";

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
    public EndpointTask createEndpointTask() {
        EndpointTask task = new EndpointTask();
        task.setDnsName(vmName);
        endpointTasks.add(task);
        return task;
    }

    //----------------------------------------------------------------------------------------------
    private boolean isValidString(String s) {
        return !(s == null || "".equals(s));
    }

    //----------------------------------------------------------------------------------------------
    public List<String> makeCommandList() {
        List<String> result = new ArrayList<String>();
        result.add("vm");
        if(isValidString(roleFileName)) {
            result.add("create-from");
            result.add(addUuid ? vmName + "-" + uuid : vmName);
            result.add(roleFileName);
        }
        else {
            result.add("create");
            result.add(addUuid ? vmName + "-" + uuid : vmName);
            result.add(imageName);
            result.add(user);
            result.add(password);
            result.add("--size");
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
        result.add("--location");
        result.add(location);

        if(isValidString(virtualNetworkName)) {
            result.add("--virtual-network-name");
            result.add(virtualNetworkName);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
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

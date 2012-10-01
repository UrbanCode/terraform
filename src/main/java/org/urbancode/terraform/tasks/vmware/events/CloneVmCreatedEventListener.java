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
package org.urbancode.terraform.tasks.vmware.events;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.ExtensionTask;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.util.IOUtil;
import org.urbancode.terraform.tasks.vmware.CloneTask;
import org.urbancode.terraform.tasks.vmware.EnvironmentTaskVmware;
import org.urbancode.terraform.tasks.vmware.PortRangeTask;
import org.urbancode.terraform.tasks.vmware.PostCreateTask;
import org.urbancode.terraform.tasks.vmware.RouterConfigPostCreateTask;
import org.urbancode.terraform.tasks.vmware.SecurityGroupRefTask;
import org.urbancode.terraform.tasks.vmware.SecurityGroupTask;
import org.urbancode.terraform.tasks.vmware.util.GlobalIpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.Ip4;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.vmware.vim25.mo.VirtualMachine;

public class CloneVmCreatedEventListener extends ExtensionTask implements TaskEventListener {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(CloneVmCreatedEventListener.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVmware environment;
    private CloneTask routerTask;
    private VirtualMachine router;

    private String routerUser = null;
    private String routerPassword = null;
    private String gateway = null;

    private CloneTask instanceTask;
    private VirtualMachine instance;
    private List<SecurityGroupTask> securityGroups = new ArrayList<SecurityGroupTask>();

    private String hostNetworkIp;
    private String privateIp;

    int virtualInterfaceNum = 0;
    private int portOffset = 10000;

    String tempConfDir;

    //----------------------------------------------------------------------------------------------
    public CloneVmCreatedEventListener() {
    }

    //----------------------------------------------------------------------------------------------
    public CloneVmCreatedEventListener(CloneTask routerTask) {
        this.environment = routerTask.fetchEnvironment();
        this.routerTask = routerTask;
        this.tempConfDir = System.getenv("TERRAFORM_HOME") +
                File.separator + "temp" + "-" + environment.fetchUUID() + File.separator;
    }

    //----------------------------------------------------------------------------------------------
    public String getGateway() {
        return gateway;
    }

    //----------------------------------------------------------------------------------------------
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setValues(CloneTask cloneTask) {
        this.environment = cloneTask.fetchEnvironment();
        this.routerTask = cloneTask;
        this.routerUser = routerTask.getUser();
        this.routerPassword = routerTask.fetchPassword();
        this.tempConfDir = System.getenv("TERRAFORM_HOME") +
                File.separator + "temp" + "-" + environment.fetchUUID() + File.separator;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void handleEvent(TaskEvent event) {
        this.tempConfDir = System.getenv("TERRAFORM_HOME") +
                File.separator + "temp" + "-" + environment.fetchUUID() + File.separator;

        SubTask subTask = event.getSubTask();
        CloneTask cloneTask = null;

        //for configuring non-router clones only
        if (subTask instanceof CloneTask) {
            cloneTask = (CloneTask) subTask;
            if (!subTask.equals(routerTask)) {
                routerUser = routerTask.getUser();
                routerPassword = routerTask.fetchPassword();
                try {
                    cloneTask.powerOnVm();
                }
                catch (Exception e) {
                   log.warn("Exception while powering on VM " + cloneTask.getInstanceName(), e);
                }

                if (cloneTask.getAssignHostIp() || cloneTask.getAssignPrivateIpOnly()) {
                    try {
                        //update VMs and security group
                        this.instanceTask = cloneTask;
                        for (SecurityGroupRefTask sgr : instanceTask.getSecurityGroupRefs()) {
                            this.securityGroups.add(sgr.fetchSecurityGroup());
                        }
                        this.router = routerTask.fetchVm();
                        this.instance = instanceTask.fetchVm();

                        //get private ip
                        VirtualHost host = environment.fetchVirtualHost();
                        host.waitForIp(instance);
                        privateIp = instance.getGuest().getIpAddress();

                        log.info("configuring private networking for VM " + cloneTask.getInstanceName());
                        if (cloneTask.getAssignPrivateIpOnly()) {
                            try {
                                //update the iptables in the router
                                addInstanceToIpTables(null, privateIp);
                                runCommand(routerUser, routerPassword, "runProgramInGuest", "/usr/sbin/service", "networking", "stop");
                                runCommand(routerUser, routerPassword, "runProgramInGuest", "/usr/sbin/service", "networking", "start");
                                cloneTask.setPrivateIp(privateIp);
                            }
                            catch (IOException e) {
                                log.warn("IOException while configuring networking (probably invalid file path)", e);
                            }
                            catch (InterruptedException e) {
                                log.warn("InterruptedException while configuring networking", e);
                            }
                        }
                        else {
                            //get host ip
                            Ip4 ip = GlobalIpAddressPool.getInstance().allocateIp();
                            this.hostNetworkIp = ip.toString();
                            configureNetworking();
                        }
                    }
                    catch (RemoteException e) {
                        log.warn("RemoteException while waiting for IP address", e);
                    }
                    catch (InterruptedException e) {
                        log.warn("InterruptedException while waiting for IP address", e);
                    }
                }
            }
        }
    }

  //----------------------------------------------------------------------------------------------
    public void configureNetworking() {
        try {
            //update interfaces, then bring interface down and up
            addNewEntryToInterfaces("" + virtualInterfaceNum);

            //update the iptables in the router
            addInstanceToIpTables(hostNetworkIp, privateIp);
            virtualInterfaceNum++;
            instanceTask.setPublicIp(hostNetworkIp);
            instanceTask.setPrivateIp(privateIp);
        }
        catch (IOException e) {
            log.warn("IOException while configuring networking (probably invalid file path)", e);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException while configuring networking", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void addInstanceToIpTables(String networkIp, String privateIp)
    throws IOException, InterruptedException {
        String result = null;
        String hostIpTablesPath = this.tempConfDir + "iptables.conf";
        String guestIpTablesPath = "/etc/iptables.conf";

        copyFileFromGuestToHost(guestIpTablesPath, hostIpTablesPath);
        String iptables = FileUtils.readFileToString(new File(hostIpTablesPath));
        String commentString = "#Instance Prerouting Tables";

        //no public IP will be assigned
        if(networkIp == null) {
            String ipTablesString = commentString;
            for (SecurityGroupTask securityGroup : this.securityGroups) {
                for (PortRangeTask prt : securityGroup.getPortRanges()) {
                    ipTablesString = ipTablesString + "\n" +
                    createIpTablesStringForVm(privateIp, prt.getFirstPort(), prt.getLastPort());
                }
            }
            result = iptables.replace(commentString, ipTablesString);
        }
        else {
            if (this.securityGroups.isEmpty()) {
                String ipTablesString = commentString + "\n" + createDefaultIpTablesStringForVm(networkIp, privateIp);
                result = iptables.replace(commentString, ipTablesString);
            }
            else {
                String ipTablesString = commentString;
                for (SecurityGroupTask securityGroup : this.securityGroups) {
                    for (PortRangeTask prt : securityGroup.getPortRanges()) {
                        ipTablesString = ipTablesString + "\n" +
                        createIpTablesStringForVm(networkIp, privateIp, prt.getFirstPort(), prt.getLastPort());
                    }
                }
                result = iptables.replace(commentString, ipTablesString);
            }
        }

        //trailing newline is necessary for iptables (commons-io removes it when file is read)
        iptables = iptables + "\n";

        if (result== null || result.equalsIgnoreCase(iptables)) {
            throw new IOException();
        }

        FileWriter out = new FileWriter(hostIpTablesPath, false);
        out.write(result);
        out.close();

        copyFileFromHostToGuest(hostIpTablesPath, guestIpTablesPath);

        runCommand(routerUser, routerPassword, "runProgramInGuest", "/bin/sh", "-c",
                "\"/sbin/iptables-restore </etc/iptables.conf\"");
    }

    //----------------------------------------------------------------------------------------------
    private void addNewEntryToInterfaces(String ifaceName)
    throws IOException, InterruptedException {
        String result = null;
        String hostInterfacesPath = this.tempConfDir + "interfaces";
        String guestInterfacesPath = "/etc/network/interfaces";
        log.debug("host interfaces path: " + hostInterfacesPath);
        copyFileFromGuestToHost(guestInterfacesPath, hostInterfacesPath);
        String interfaces = FileUtils.readFileToString(new File(hostInterfacesPath));
        String commentString = "#Insert New Interfaces";
        String interfacesString = commentString + "\n" + createInterfaceString(hostNetworkIp, ifaceName);
        result = interfaces.replace(commentString, interfacesString);

        if (result== null || result.equalsIgnoreCase(interfaces)) {
            throw new IOException();
        }

        FileWriter out = new FileWriter(hostInterfacesPath, false);
        out.write(result);
        out.close();

        copyFileFromHostToGuest(hostInterfacesPath, guestInterfacesPath);

        //restart the network
        runCommand(routerUser, routerPassword, "runProgramInGuest", "/usr/sbin/service", "networking", "stop");
        runCommand(routerUser, routerPassword, "runProgramInGuest", "/usr/sbin/service", "networking", "start");
    }

    //----------------------------------------------------------------------------------------------
    private String createIpTablesStringForVm(String privateIp, int beginPort, int endPort) {
        String routerIp = fetchRouterIp();
        //example string
        //-A PREROUTING -d 10.15.50.1/32 -p tcp -m tcp --dport 10022 -j DNAT --to-destination 192.168.0.2:22
        String result = "";

        //dport command has colon delimiter while to-destination command has hyphen delimiter
        String dPorts;
        String ports;
        int beginWithOffset = beginPort + portOffset;
        int endWithOffset = endPort + portOffset;
        if (beginPort == endPort) {
            dPorts = "" + beginWithOffset;
            ports = "" + beginPort;
        }
        else {
            dPorts = "" + beginWithOffset + ":" + endWithOffset;
            ports = "" + beginPort + "-" + endPort;
        }
        result = result + "-A PREROUTING -d " + routerIp +
                "/32 -p tcp -m tcp --dport " + dPorts + " -j DNAT --to-destination " + privateIp + ":" + ports;
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private String createIpTablesStringForVm(String networkIp, String privateIp, int beginPort, int endPort) {
        //example string
        //-A PREROUTING -d 10.15.50.2/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 192.168.0.2:22
        String result = "";

        //dport command has colon delimiter while to-destination command has hyphen delimiter
        String dPorts;
        String ports;
        if (beginPort == endPort) {
            dPorts = "" + beginPort;
            ports = "" + beginPort;
        }
        else {
            dPorts = "" + beginPort + ":" + endPort;
            ports = "" + beginPort + "-" + endPort;
        }
        result = result + "-A PREROUTING -d " + networkIp +
                "/32 -p tcp -m tcp --dport " + dPorts + " -j DNAT --to-destination " + privateIp + ":" + ports;
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private String createDefaultIpTablesStringForVm (String networkIp, String privateIp) {
        //example string
        //-A PREROUTING -d 10.15.50.2/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 192.168.0.2:22
        String result = "";
        result = result + "-A PREROUTING -d " + networkIp +
                "/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination " + privateIp + ":22" + "\n";
        result = result + "-A PREROUTING -d " + networkIp +
                "/32 -p tcp -m tcp --dport 80 -j DNAT --to-destination " + privateIp + ":80" + "\n";
        result = result + "-A PREROUTING -d " + networkIp +
                "/32 -p tcp -m tcp --dport 3306 -j DNAT --to-destination " + privateIp + ":3306";
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private String createInterfaceString(String ip, String ifaceName) {
        String result;
        if(gateway == null | "".equals(gateway)) {
            log.warn("Gateway is null or emptyand was not set properly.");
        }
        result = "auto eth0:" + ifaceName + "\n";
        result = result + "iface eth0:" + ifaceName + " inet static" + "\n";
        result = result + "address " + ip + "\n";
        result = result + "gateway " + gateway + "\n";
        result = result + "netmask 255.255.0.0" + "\n\n";

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private String fetchRouterIp() {
        String result = null;
        List<PostCreateTask> pcTasks = routerTask.getPostCreateTaskList();
        for(PostCreateTask pcTask : pcTasks) {
            if (pcTask instanceof RouterConfigPostCreateTask) {
                result = ((RouterConfigPostCreateTask) pcTask).fetchRouterIp();
                break;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String user, String password, String vmRunCommand, String... args)
    throws IOException, InterruptedException {
        runCommand(user, password, vmRunCommand, Arrays.asList(args));
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String vmUser, String vmPassword, String vmRunCommand, List<String> args)
    throws IOException, InterruptedException {
        if(vmUser == null || vmPassword == null) {
            log.error("Either VM user or password were null. " +
                    "They need to be specified in the template under the clone element.");
            throw new NullPointerException();
        }
        VirtualHost host = environment.fetchVirtualHost();
        host.waitForVmtools(router);
        String vmx = host.getVmxPath(router);
        String url = host.getUrl();
        String virtualHostUser = host.getUser();
        String virtualHostPassword = host.getPassword();
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("vmrun");
        commandLine.add("-T");
        commandLine.add("server");
        commandLine.add("-h");
        commandLine.add(url);
        commandLine.add("-u");
        commandLine.add(virtualHostUser);
        commandLine.add("-p");
        commandLine.add(virtualHostPassword);
        commandLine.add("-gu");
        commandLine.add(vmUser);
        commandLine.add("-gp");
        commandLine.add(vmPassword);
        commandLine.add(vmRunCommand);
        commandLine.add(vmx);
        commandLine.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        InputStream procIn = process.getInputStream();
        IOUtil.getInstance().discardStream(procIn);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with code " + exitCode);
        }
        log.info("ran command " + vmRunCommand + " " + args.get(0));
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromGuestToHost(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(destination);
        String absDestination = temp.getAbsolutePath();
        args.add(origin);
        args.add(absDestination);
        runCommand(routerUser, routerPassword, "copyFileFromGuestToHost", args);
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromHostToGuest(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(origin);
        String absOrigin = temp.getAbsolutePath();
        args.add(absOrigin);
        args.add(destination);
        runCommand(routerUser, routerPassword, "copyFileFromHostToGuest", args);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
    }
}

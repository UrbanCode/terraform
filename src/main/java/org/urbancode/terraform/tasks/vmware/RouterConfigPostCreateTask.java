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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.util.GlobalIpAddressPool;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.vmware.vim25.Description;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class RouterConfigPostCreateTask extends PostCreateTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(RouterConfigPostCreateTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String gateway;
    private String dns;
    private String routerIp = null;

    //----------------------------------------------------------------------------------------------
    public RouterConfigPostCreateTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public RouterConfigPostCreateTask(CloneTask cloneTask) {
        super(cloneTask);
    }

    //----------------------------------------------------------------------------------------------
    public String getGateway() {
        return gateway;
    }

    //----------------------------------------------------------------------------------------------
    public String getDns() {
        return dns;
    }

    //----------------------------------------------------------------------------------------------
    public String fetchRouterIp() {
        return routerIp;
    }

    //----------------------------------------------------------------------------------------------
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    //----------------------------------------------------------------------------------------------
    public void setDns(String dns) {
        this.dns = dns;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Configures a Debian-based router after it has been created.
     * Copies over the isc-dhcp-server, iptables, dhcpd, and interfaces configuration files.
     * The host machine running Terraform must have VMRun installed.
     * This method ends when the router successfully broadcasts its IP address.
     * If it does not broadcast an IP address this method will time out after 10 minutes.
     */
    @Override
    public void create() {
        //set VM now that the VM has been created
        this.vmToConfig = this.cloneTask.fetchVm();
        try {
            File configDir = new File(tempConfDirNoSeparator);
            configDir.mkdirs();
            copyTempFiles();
            addFirstInterface(tempConfDir + "interfaces.temp", tempConfDir + "interfaces");
            handleNetworkRefs();

            //power on vm
            cloneTask.powerOnVm();

            //bring down networking
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/usr/sbin/service", "networking",
            "stop");

            //copy networking files to router
            copyFileFromHostToGuest(tempConfDir + "isc-dhcp-server", "/etc/default/isc-dhcp-server");
            copyFileFromHostToGuest(tempConfDir + "iptables.conf", "/etc/iptables.conf");
            copyFileFromHostToGuest(tempConfDir + "dhcpd.conf", "/etc/dhcp/dhcpd.conf");
            copyFileFromHostToGuest(tempConfDir + "interfaces", "/etc/network/interfaces");

            //start networking and dhcp service
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/usr/sbin/service", "networking",
            "start");
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/sbin/insserv", "isc-dhcp-server");
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/usr/sbin/service", "isc-dhcp-server", "start");

            VirtualHost host = environment.fetchVirtualHost();
            host.waitForIp(vmToConfig);
        }
        catch (IOException e) {
            log.warn("Failed to load file while configuring router", e);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException while configuring router", e);
        }
        catch (Exception e) {
            log.warn("Unknown exception while configuring router", e);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        File configDir = new File(tempConfDirNoSeparator);
        try {
            log.debug("deleting conf directory");
            FileUtils.deleteDirectory(configDir);
        } catch (IOException e) {
            log.warn("Unable to delete conf directory", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void copyTempFiles() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String cpDir = "org/urbancode/terraform/conf" + File.separator;
        InputStream iptablesStream = loader.getResourceAsStream(cpDir + "iptables.conf.temp");
        InputStream dhcpdStream = loader.getResourceAsStream(cpDir + "dhcpd.conf.temp");
        InputStream interfacesStream = loader.getResourceAsStream(cpDir + "interfaces.temp");
        InputStream iscStream = loader.getResourceAsStream(cpDir + "isc-dhcp-server.temp");

        try {
            writeInputStreamToFile(iptablesStream, tempConfDir + "iptables.conf.temp");
            writeInputStreamToFile(iptablesStream, tempConfDir + "dhcpd.conf.temp");
            writeInputStreamToFile(iptablesStream, tempConfDir + "interfaces.temp");
            writeInputStreamToFile(iptablesStream, tempConfDir + "isc-dhcp-server.temp");
        }
        catch(IOException e) {
            iptablesStream.close();
            dhcpdStream.close();
            interfacesStream.close();
            iscStream.close();
        }
    }

    //----------------------------------------------------------------------------------------------
    private void writeInputStreamToFile(InputStream inStream, String filePath) throws IOException {
        File outFile = new File(filePath);
        OutputStream out=new FileOutputStream(outFile);
        byte buf[]=new byte[1024];
        int len;
        try {
            while ((len=inStream.read(buf))>0) {
                out.write(buf,0,len);
            }
        }
        catch(IOException e) {
            log.warn("IOException while copying to file " + filePath, e);
        }
        finally {
            out.close();
            inStream.close();
        }
    }

    //----------------------------------------------------------------------------------------------
    public void addFirstInterface(String inFileName, String outFileName)
    throws IOException {
        GlobalIpAddressPool ipPool = GlobalIpAddressPool.getInstance();
        routerIp = ipPool.allocateIp().toString();

        String ifaces = FileUtils.readFileToString(new File(inFileName));
        ifaces = ifaces + "\n\nauto eth0\n"
                        + "allow-hotplug eth0\n"
                        + "iface eth0 inet static\n"
                        + "  address " + routerIp + "\n"
                        + "  gateway " + gateway + "\n"
                        + "  netmask 255.255.0.0\n"
                        + "#Insert New Interfaces\n";
        writeToFile(outFileName, ifaces, false);
    }

    //----------------------------------------------------------------------------------------------
    public void handleNetworkRefs()
    throws NetworkConfigurationException, InterruptedException, IOException {
        //create network cards (VM must be powered off)
        List<Integer> nicIndexes = new ArrayList<Integer>();
        boolean first = true;
        int subnetNum = 0;
        List<NetworkRefTask> netRefs = cloneTask.getNetworkRefs();
        for (NetworkRefTask netRef : netRefs) {
            nicIndexes.add(netRef.getNicIndex());
        }
        for (NetworkRefTask netRef : netRefs) {
            String netName = netRef.fetchSwitch().getSwitchPath().getName();
            int nicIndex = netRef.getNicIndex();
            int netAdapterNum = nicIndex + 1;
            String nicName = "Network adapter " + netAdapterNum;
            addNewNetworkCard(vmToConfig, netName, nicName);
            netRef.attachNic();
            //add new interface/subnet/network to iptables, interfaces, and dhcpd
            String iptablesIn;
            String interfacesIn;
            String dhcpdIn;
            if (first) {
                iptablesIn = tempConfDir + "iptables.conf.temp";
                interfacesIn = tempConfDir + "interfaces";
                dhcpdIn = tempConfDir + "dhcpd.conf.temp";
                first = false;
            }
            else {
                iptablesIn = tempConfDir + "iptables.conf";
                interfacesIn = tempConfDir + "interfaces";
                dhcpdIn = tempConfDir + "dhcpd.conf";
            }
            String iptablesOut = tempConfDir + "iptables.conf";
            String interfacesOut = tempConfDir + "interfaces";
            String dhcpdOut = tempConfDir + "dhcpd.conf";

            nicIndexes.remove(new Integer(nicIndex));

            addIfaceToIptables(nicIndex, nicIndexes, iptablesIn, iptablesOut);
            addInterface(nicIndex, subnetNum, interfacesIn, interfacesOut);
            addSubnetToDhcpd(subnetNum, dhcpdIn, dhcpdOut);
            nicIndexes.add(nicIndex);
            subnetNum++;
        }

        //edit default dhcp interfaces file
        String ifacesString = createDhcpInterfacesString(nicIndexes);
        String inFileName = tempConfDir + "isc-dhcp-server.temp";
        String outFileName = tempConfDir + "isc-dhcp-server";
        createDhcpInterfacesFile(ifacesString, inFileName, outFileName);
    }

    //----------------------------------------------------------------------------------------------
    public void addNewNetworkCard(VirtualMachine vm, String netName, String nicName)
    throws NetworkConfigurationException {
        try {
            VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
            VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName);
            vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[] {nicSpec});
            Task task = vm.reconfigVM_Task(vmSpec);
            @SuppressWarnings("unused")
            String result = task.waitForTask();
        }
        catch(Exception e) {
            throw new NetworkConfigurationException("Exception while adding network card to VM: " +
            e.getClass().getCanonicalName(), e);
        }

    }

    //----------------------------------------------------------------------------------------------
    public VirtualDeviceConfigSpec createNicSpec(String netName, String nicName) {
        //create the specs for the new virtual ethernet card
        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

        VirtualEthernetCard nic =  new VirtualE1000();

        VirtualEthernetCardNetworkBackingInfo nicBacking =
        new VirtualEthernetCardNetworkBackingInfo();
        nicBacking.setDeviceName(netName);

        VirtualDeviceConnectInfo connectInfo = new VirtualDeviceConnectInfo();
        connectInfo.setConnected(true);
        connectInfo.setStartConnected(true);
        nic.setConnectable(connectInfo);

        Description info = new Description();
        info.setLabel(nicName);
        info.setSummary(netName);
        nic.setDeviceInfo(info);

        // allowable types: "generated", "manual", "assigned"
        nic.setAddressType("generated");
        nic.setBacking(nicBacking);
        //according to vsphere api, keys should be unique, but this does not appear to be enforced
        nic.setKey(0);

        nicSpec.setDevice(nic);
        return nicSpec;
    }

    //----------------------------------------------------------------------------------------------
    public void addIfaceToIptables(
            int nicIndex,
            List<Integer> excludedIndexes,
            String inFileName,
            String outFileName)
    throws IOException {
        //add rules above last line of iptables.conf file
        String eth = "eth" + nicIndex;
        String inboundRule = "-A FORWARD -i eth0 -o " + eth + " -m state --state RELATED,ESTABLISHED -j ACCEPT";
        String outboundRule = "-A FORWARD -i " + eth + " -o eth0 -j ACCEPT";

        String iptables = FileUtils.readFileToString(new File(inFileName));
        String[] split = iptables.split("\n");
        String lastLine = split[split.length - 1];
        lastLine = inboundRule + "\n" + outboundRule + "\n" + lastLine;

        for (Integer i : excludedIndexes) {
            String exEth = "eth" + i.toString();
            String inboundReject = "-A FORWARD -i " + exEth + " -o " + eth + " -j REJECT";
            String outboundReject = "-A FORWARD -i " + eth + " -o " + exEth + " -j REJECT";
            lastLine = inboundReject + "\n" + outboundReject + "\n" + lastLine;
        }

        split[split.length - 1] = lastLine;
        iptables = join(split, "\n");
        //trailing newline is necessary for iptables (commons-io removes it when file is read)
        iptables = iptables + "\n";

        writeToFile(outFileName, iptables, false);
    }

    //----------------------------------------------------------------------------------------------
    public String createDhcpInterfacesString(List<Integer> nicIndexes) {
        //constructs content for isc-dhcp-server file
        //example: INTERFACES="eth1 eth2" (with quotes)
        String result = "INTERFACES=\"";
        boolean first = true;
        for (Integer i : nicIndexes) {
            if (first) {
                result = result + "eth" + i.toString();
                first = false;
            }
            else {
                result = result + " eth" + i.toString();
            }
        }
        result = result + "\"";
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void createDhcpInterfacesFile(String ifacesString, String oldFileName, String newFileName)
    throws IOException {
        //create isc-dhcp-server file string
        String ifacesFileAsString = FileUtils.readFileToString(new File(oldFileName));
        String result = ifacesFileAsString.replace("INTERFACES=\"\"", ifacesString);

        writeToFile(newFileName, result, false);
    }

    //----------------------------------------------------------------------------------------------
    public void addInterface(int nicIndex, int subnetNum, String inFileName, String outFileName)
    throws IOException {
        //add new interface to /etc/network/interfaces file
        String eth = "eth" + nicIndex;
        String ifaces = FileUtils.readFileToString(new File(inFileName));
        ifaces = ifaces + "\nauto " + eth + "\n"
                + "allow-hotplug " + eth + "\n"
                + "iface " + eth + " inet static\n"
                + "  address 192.168." + subnetNum + ".1\n"
                + "  netmask 255.255.255.0\n";
        writeToFile(outFileName, ifaces, false);
    }

    //----------------------------------------------------------------------------------------------
    public void addSubnetToDhcpd(int subnetNum, String inFileName, String outFileName)
    throws IOException {
        //add new subnet to dhcpd.conf file
        String dhcpd = FileUtils.readFileToString(new File(inFileName));
        dhcpd = dhcpd + "\nsubnet 192.168." + subnetNum + ".0 netmask 255.255.255.0 {\n"
            + "use-host-decl-names on;\n"
            + "option routers 192.168." + subnetNum + ".1;\n"
            + "option domain-name-servers " + dns + ";\n"
            + "pool {\n"
            + "range 192.168." + subnetNum + ".2 192.168." + subnetNum + ".250;\n"
            + "}\n"
            + "}\n";
        writeToFile(outFileName, dhcpd, false);
    }


}

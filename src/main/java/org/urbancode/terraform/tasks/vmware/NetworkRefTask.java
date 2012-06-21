package org.urbancode.terraform.tasks.vmware;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.vmware.util.SwitchResult;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.mo.VirtualMachine;

public class NetworkRefTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(NetworkRefTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private int nicIndex;
    private String networkName;
    private VirtualMachine vm = null;
    private VirtualHost host = null;
    private SwitchResult vSwitch = null;

    //----------------------------------------------------------------------------------------------
    public NetworkRefTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public int getNicIndex() {
        return this.nicIndex;
    }

    //----------------------------------------------------------------------------------------------
    public String getNetworkName() {
        return this.networkName;
    }

    //----------------------------------------------------------------------------------------------
    public SwitchResult fetchSwitch() {
        return this.vSwitch;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualMachine(VirtualMachine vm) {
        this.vm = vm;
    }

    //----------------------------------------------------------------------------------------------
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    //----------------------------------------------------------------------------------------------
    public void setNicIndex(int nicIndex) {
        this.nicIndex = nicIndex;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualSwitch(SwitchResult vSwitch) {
        this.vSwitch = vSwitch;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualHost(VirtualHost host) {
        this.host = host;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        try {
            attachNic();
        }
        catch (RemoteException e) {
            log.warn("RemoteException while attaching NIC", e);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException while attaching NIC", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    //----------------------------------------------------------------------------------------------
    public void attachNic()
    throws RemoteException, InterruptedException {
        try {
            List<VirtualEthernetCard> nics = host.getAllNics(vm);
            if (nicIndex < nics.size()) {
                VirtualEthernetCard nic = host.getNic(vm, nicIndex);
                host.attachNic(vm, nic, vSwitch.getNetwork());
            }
        }
        catch (IndexOutOfBoundsException e) {
            log.warn("Tried to attach NIC before card was created. NIC not attached.");
        }
        catch (IllegalArgumentException e) {
            log.warn("Tried to attach NIC before card was created. NIC not attached.");
        }

    }

}

package com.urbancode.uprovision.tasks.vmware.util;

import java.io.Serializable;

import com.urbancode.commons.util.Check;
import com.vmware.vim25.mo.Network;

public class SwitchResult implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private Network network;
    final private Path switchPath;
    final private Path portGroupPath;

    //----------------------------------------------------------------------------------------------
    public SwitchResult(Network network, Path switchPath, Path portGroupPath) {
        Check.nonNull(network, "network");
        Check.nonNull(switchPath, "switchPath");
        Check.nonNull(portGroupPath, "portGroupPath");
        this.network = network;
        this.switchPath = switchPath;
        this.portGroupPath = portGroupPath;
    }

    //----------------------------------------------------------------------------------------------
    public Network getNetwork() {
        return network;
    }

    //----------------------------------------------------------------------------------------------
    public Path getSwitchPath() {
        return switchPath;
    }
    //----------------------------------------------------------------------------------------------
    public Path getPortGroupPath() {
        return portGroupPath;
    }
}

package org.urbancode.terraform.tasks.vmware;

import org.urbancode.terraform.tasks.common.SubTask;

public class PortRangeTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private int firstPort;
    private int lastPort;

    //----------------------------------------------------------------------------------------------
    public PortRangeTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public PortRangeTask(int firstPort, int lastPort) {
        super();
        this.firstPort = firstPort;
        this.lastPort = lastPort;
    }

    //----------------------------------------------------------------------------------------------
    public int getFirstPort() {
        return firstPort;
    }

    //----------------------------------------------------------------------------------------------
    public int getLastPort() {
        return lastPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setFirstPort(int firstPort) {
        this.firstPort = firstPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setLastPort(int lastPort) {
        if(lastPort > this.firstPort) {
            this.lastPort = lastPort;
        }
        else {
            this.lastPort = this.firstPort;
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}

package org.urbancode.terraform.tasks.vmware;

import java.util.ArrayList;
import java.util.List;

import org.urbancode.terraform.tasks.common.SubTask;


public class SecurityGroupTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String name;
    private List<PortRangeTask> portRanges = new ArrayList<PortRangeTask>();

    //----------------------------------------------------------------------------------------------
    public SecurityGroupTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return this.name;
    }

    //----------------------------------------------------------------------------------------------
    public List<PortRangeTask> getPortRanges() {
        return this.portRanges;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public PortRangeTask createPortRange() {
        PortRangeTask result = new PortRangeTask();
        portRanges.add(result);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
    }

}

package org.urbancode.terraform.commands.vmware;

import org.urbancode.terraform.commands.common.Command;
import org.urbancode.terraform.commands.common.CommandException;
import org.urbancode.terraform.tasks.vmware.ContextVmware;

public class TakeSnapshotCommand implements Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;

    //----------------------------------------------------------------------------------------------
    public TakeSnapshotCommand(ContextVmware context) {
        this.context = context;
    }


    //----------------------------------------------------------------------------------------------
    @Override
    public void execute()
    throws CommandException {
        // TODO this command take a snapshot of all VMs in the environment

    }

}

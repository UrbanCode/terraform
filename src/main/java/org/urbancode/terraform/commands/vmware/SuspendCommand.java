package org.urbancode.terraform.commands.vmware;

import org.urbancode.terraform.commands.common.Command;

public class SuspendCommand extends Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public SuspendCommand() {
        super();
    }
    @Override
    public void execute() throws Exception {
        // TODO suspend all VMs in the reverse order that they were created
    }

}

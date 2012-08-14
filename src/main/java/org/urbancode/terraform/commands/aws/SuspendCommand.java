package org.urbancode.terraform.commands.aws;

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

    //----------------------------------------------------------------------------------------------
    @Override
    public void execute() throws Exception {
        // TODO this command will stop (not delete) all instances in an environment
    }

}

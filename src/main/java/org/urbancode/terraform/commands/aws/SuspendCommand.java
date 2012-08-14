package org.urbancode.terraform.commands.aws;

import org.urbancode.terraform.commands.common.Command;
import org.urbancode.terraform.commands.common.CommandException;
import org.urbancode.terraform.tasks.aws.ContextAWS;

public class SuspendCommand implements Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextAWS context;

    //----------------------------------------------------------------------------------------------
    public SuspendCommand(ContextAWS context) {
        this.context = context;
    }


    //----------------------------------------------------------------------------------------------
    @Override
    public void execute()
    throws CommandException {
        // TODO this command will stop (not delete) all instances in an environment

    }

}
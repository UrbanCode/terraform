package org.urbancode.terraform.commands.vmware;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.commands.common.Command;
import org.urbancode.terraform.commands.common.CommandException;
import org.urbancode.terraform.tasks.vmware.CloneTask;
import org.urbancode.terraform.tasks.vmware.ContextVmware;
import org.urbancode.terraform.tasks.vmware.EnvironmentTaskVmware;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;

public class ResumeCommand implements Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ResumeCommand.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;

    //----------------------------------------------------------------------------------------------
    public ResumeCommand(ContextVmware context) {
        this.context = context;
    }


    //----------------------------------------------------------------------------------------------
    /**
     * Attempts to power on all powered off and suspended VMs in the environment.
     * This will be done in the order the VMs were created.
     */
    @Override
    public void execute()
    throws CommandException {
        List<CloneTask> cloneTasks = fetchCloneTaskList();
        Collections.sort(cloneTasks);
        for(CloneTask clone : cloneTasks) {
            try {
                clone.powerOnVm();
            } catch (InvalidProperty e) {
                log.warn("InvalidProperty fault when powering on VM from resume command: " + clone.getInstanceName());
                throw new CommandException();
            } catch (RuntimeFault e) {
                log.warn("RuntimeFault when powering on VM from resume command: " + clone.getInstanceName());
                throw new CommandException();
            } catch (RemoteException e) {
                log.warn("RemoteException when powering on VM from resume command: " + clone.getInstanceName());
                throw new CommandException();
            } catch (InterruptedException e) {
                log.warn("InterruptedException when powering on VM from resume command: " + clone.getInstanceName());
                throw new CommandException();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private List<CloneTask> fetchCloneTaskList() {
        return ((EnvironmentTaskVmware) context.getEnvironment()).getCloneTasks();
    }

}

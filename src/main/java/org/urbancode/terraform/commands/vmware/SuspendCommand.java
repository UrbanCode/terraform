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

public class SuspendCommand implements Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(SuspendCommand.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;

    //----------------------------------------------------------------------------------------------
    public SuspendCommand(ContextVmware context) {
        this.context = context;
    }


    //----------------------------------------------------------------------------------------------
    /**
     * Attempts to suspend power (yellow pause icon) on all VMs in the environment.
     * This will be executed in reverse from the order the VMs were created.
     */
    @Override
    public void execute()
    throws CommandException {
        List<CloneTask> cloneTasks = fetchCloneTaskList();
        reverseOrderCloneList(cloneTasks);
        for(CloneTask clone : cloneTasks) {
            try {
                clone.suspendVm();
            } catch (InvalidProperty e) {
                log.warn("InvalidProperty fault when suspending power on VM: " + clone.getInstanceName());
                throw new CommandException();
            } catch (RuntimeFault e) {
                log.warn("RuntimeFault when suspending power on VM: " + clone.getInstanceName());
                throw new CommandException();
            } catch (RemoteException e) {
                log.warn("RemoteException when suspending power on VM: " + clone.getInstanceName());
                throw new CommandException();
            } catch (InterruptedException e) {
                log.warn("InterruptedException when suspending power on VM: " + clone.getInstanceName());
                throw new CommandException();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void reverseOrderCloneList(List<CloneTask> cloneTasks) {
        Collections.sort(cloneTasks);
        Collections.reverse(cloneTasks);
    }

    //----------------------------------------------------------------------------------------------
    private List<CloneTask> fetchCloneTaskList() {
        return ((EnvironmentTaskVmware) context.getEnvironment()).getCloneTasks();
    }

}

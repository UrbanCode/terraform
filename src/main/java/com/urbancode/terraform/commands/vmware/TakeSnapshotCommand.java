package com.urbancode.terraform.commands.vmware;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.urbancode.terraform.commands.common.Command;
import com.urbancode.terraform.commands.common.CommandException;
import com.urbancode.terraform.tasks.vmware.CloneTask;
import com.urbancode.terraform.tasks.vmware.ContextVmware;
import com.urbancode.terraform.tasks.vmware.EnvironmentTaskVmware;
import com.vmware.vim25.VimFault;

public class TakeSnapshotCommand implements Command {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(TakeSnapshotCommand.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;

    //----------------------------------------------------------------------------------------------
    public TakeSnapshotCommand(ContextVmware context) {
        this.context = context;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Attempts to take a snapshot of every VM in the specified environment.
     * @throws CommandException
     */
    @Override
    public void execute()
    throws CommandException {
        List<CloneTask> cloneTasks = fetchCloneTaskList();
        Collections.sort(cloneTasks);
        for(CloneTask clone : cloneTasks) {
            try {
                clone.takeSnapshotOfVm();
            } catch (VimFault e) {
                log.warn("vSphere faulted when taking snapshot of VM: " + clone.getInstanceName());
                throw new CommandException();
            } catch (RemoteException e) {
                log.warn("RemoteException when taking snapshot of VM: " + clone.getInstanceName());
                throw new CommandException();
            } catch (InterruptedException e) {
                log.warn("InterruptedException when taking snapshot of VM: " + clone.getInstanceName());
                throw new CommandException();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private List<CloneTask> fetchCloneTaskList() {
        return ((EnvironmentTaskVmware) context.getEnvironment()).getCloneTasks();
    }

}

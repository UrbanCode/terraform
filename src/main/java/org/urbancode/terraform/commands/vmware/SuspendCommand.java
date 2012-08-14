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
            } catch (RuntimeFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void reverseOrderCloneList(List<CloneTask> cloneTasks) {
        Collections.sort(cloneTasks);
        Collections.reverse(cloneTasks);
    }

    //----------------------------------------------------------------------------------------------
    public List<CloneTask> fetchCloneTaskList() {
        return ((EnvironmentTaskVmware) context.getEnvironment()).getCloneTasks();
    }

}

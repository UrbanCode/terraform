package org.urbancode.terraform.tasks.util;

import java.util.Comparator;

import org.urbancode.terraform.tasks.aws.InstanceTask;


// CHANGE THIS ONCE WE ABSTRACT OUT INSTANCES/CLONES

public class InstancePriorityComparator implements Comparator<InstanceTask>{

    //----------------------------------------------------------------------------------------------
    public int compare(InstanceTask inst1, InstanceTask inst2) {
        int result = 0;
        if (inst1.getPriority() == inst2.getPriority() ) {
            result = 0;
        }
        else if (inst1.getPriority() > inst2.getPriority()) {
            result = 1;
        }
        else {
            result = -1;
        }
        
        return result;
    }
}

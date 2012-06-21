package com.urbancode.uprovision.tasks.vmware.events;

import java.util.HashSet;
import java.util.Set;


public final class TaskEventService {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private Set<TaskEventListener> listeners = new HashSet<TaskEventListener>();

    //----------------------------------------------------------------------------------------------
    public TaskEventService() {
    }

    //----------------------------------------------------------------------------------------------
    synchronized public void addEventListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    //----------------------------------------------------------------------------------------------
    synchronized public void removeEventListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    //----------------------------------------------------------------------------------------------
    synchronized public void sendEvent(TaskEvent event) {
        for (TaskEventListener listener : listeners) {
            listener.handleEvent(event);
        }
    }

}

/*******************************************************************************
 * Copyright 2012 Urbancode, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.urbancode.terraform.tasks.common;

import java.util.ArrayList;
import java.util.List;


public class MultiThreadTask extends Task implements Runnable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private Task task;
    private boolean doCreate;
    private List<Exception> exceptions;

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param task - the Task that will be ran in a separate thread
     * @param doCreate - true for Task.create() or false for Task.destroy()
     * @param context - the Context of the Task
     */
    public MultiThreadTask(Task task, boolean doCreate, Context context) {
        super(context);
        this.task = task;
        this.doCreate = doCreate;
        exceptions = new ArrayList<Exception>();
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param context - the Context of the Task
     */
    public MultiThreadTask(Context context) {
        super(context);
        exceptions = new ArrayList<Exception>();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Launches a Task in a new thread, running the Task's create() or destroy()
     */
    @Override
    public void run() {
        try {
            if (doCreate) {
                task.create();
            }
            else {
                task.destroy();
            }
        }
        catch (Exception e) {
            // if we catch any exceptions, add them to our exceptions list
            exceptions.add(e);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return a list of all the caught exceptions
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() {

    }



}

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
    public MultiThreadTask(Task task, boolean doCreate, Context context) {
        super(context);
        this.task = task;
        this.doCreate = doCreate;
        exceptions = new ArrayList<Exception>();
    }
    
    //----------------------------------------------------------------------------------------------
    public MultiThreadTask(Context context) {
        super(context);
        exceptions = new ArrayList<Exception>();
    }

    //----------------------------------------------------------------------------------------------
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
            exceptions.add(e);
        }
    }
    
    //----------------------------------------------------------------------------------------------
    public List<Exception> getExceptions() {
        return exceptions;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        // TODO Auto-generated method stub
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}

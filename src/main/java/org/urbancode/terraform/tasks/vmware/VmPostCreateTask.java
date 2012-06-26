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
package org.urbancode.terraform.tasks.vmware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class VmPostCreateTask extends PostCreateTask {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VmPostCreateTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String cmds;
    
    //----------------------------------------------------------------------------------------------
    public VmPostCreateTask(CloneTask cloneTask) {
        super(cloneTask);
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCommands(String cmds) {
        this.cmds = cmds;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getCommands(String cmds) {
        return cmds;
    }
    
    //----------------------------------------------------------------------------------------------
    private void parseAndRunCmd(String cmd) 
    throws IOException, InterruptedException {
        if (cmd != null && !cmd.isEmpty()) {
            String[] splitCmd = cmd.split(" ");
            
            List<String> cmdElements = new ArrayList<String>();
            Collections.addAll(cmdElements, splitCmd);
            
            runCommand(vmUser, vmPassword, "runProgramInGuest", cmdElements);
        }
        else {
            log.warn("No command specified");
        }
    }
    
    //----------------------------------------------------------------------------------------------
    private void runCmds() {
        if (cmds != null && !cmds.isEmpty()) {
            String[] cmdsArray = cmds.split(";");
            
            for (String cmd : cmdsArray) {
                try {
                    parseAndRunCmd(cmd);
                } catch (IOException e) {
                    log.error("Unable to run command (" + cmd + ") on machine " 
                              + cloneTask.getInstanceName());
                } catch (InterruptedException e) {
                    log.error("Command interrupted (" + cmd + ") on machine " 
                            + cloneTask.getInstanceName());
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        this.context = (ContextVmware) environment.fetchContext();
        this.vmToConfig = cloneTask.fetchVm();
        
        runCmds();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
    }
    
}

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
package com.urbancode.terraform.tasks.vmware.udeploy;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.urbancode.terraform.tasks.vmware.CloneTask;
import com.urbancode.terraform.tasks.vmware.ContextVmware;
import com.urbancode.terraform.tasks.vmware.PostCreateTask;


public class UDAgentPostCreateTask extends PostCreateTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(UDAgentPostCreateTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;
    private String agentPath = "/opt/urbandeploy/agent";
    private String agentName = null;
    private String udHost = null;
    private String udPort = null;

    //----------------------------------------------------------------------------------------------
    public UDAgentPostCreateTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public UDAgentPostCreateTask(CloneTask cloneTask) {
        super();
        this.cloneTask = cloneTask;
    }

    //----------------------------------------------------------------------------------------------
    public String getAgentPath() {
        return agentPath;
    }

    //----------------------------------------------------------------------------------------------
    public String getAgentName() {
        return agentName;
    }

    //----------------------------------------------------------------------------------------------
    public String getUdHost() {
        return udHost;
    }

    //----------------------------------------------------------------------------------------------
    public String getUdPort() {
        return udPort;
    }

    //----------------------------------------------------------------------------------------------
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    //----------------------------------------------------------------------------------------------
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    //----------------------------------------------------------------------------------------------
    public void setUdHost(String udHost) {
        this.udHost = udHost;
    }

    //----------------------------------------------------------------------------------------------
    public void setUdPort(String udPort) {
        this.udPort = udPort;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        this.context = (ContextVmware) environment.fetchContext();
        this.vmToConfig = cloneTask.fetchVm();

        try {
            cloneTask.powerOnVm();
            environment.fetchVirtualHost().waitForIp(vmToConfig);
            configure();
        }
        catch (IOException e) {
            log.warn("IOException while configuring UD agent", e);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException while configuring UD agent", e);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        this.context = (ContextVmware) environment.fetchContext();
        this.vmToConfig = cloneTask.fetchVm();
        try {
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh",
                    agentPath + "/bin/" + "udagent", "stop");
            Thread.sleep(3000);
        } catch (IOException e) {
            log.info("Stopping the agent failed. This might cause an orphaned agent to appear in " +
            		"uDeploy. You will have to manually delete it.", e);
        } catch (InterruptedException e) {
            log.info("Stopping the agent failed. This might cause an orphaned agent to appear in " +
                    "uDeploy. You will have to manually delete it.", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void configure()
    throws IOException, InterruptedException {
        String udDir = "";
        if (agentPath != null && !agentPath.isEmpty()) {
            //linux agents only
            udDir = agentPath + "/bin/";

            //if not set in xml, generate agent name based on environment name and instance name
            if (agentName == null) {
                if (agentName == null || "null".equals(agentName)) {
                    //generate name based on environment name and instance name (should be unique)
                    agentName = environment.getName() + "-" + cloneTask.getInstanceName();
                }
            }

            Thread.sleep(5000);
            log.info("configuring agent with name " + agentName);
            //stop agent, rename agent, restart agent
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "udagent", "stop");
            Thread.sleep(3000);
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "configure-agent", udHost, udPort, agentName);
            Thread.sleep(3000);
            runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "udagent", "start");
        }
        else {
            log.error("No UD Agent path specified!");
        }
    }
}

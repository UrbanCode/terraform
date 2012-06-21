package org.urbancode.terraform.tasks.vmware.udeploy;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.CloneTask;
import org.urbancode.terraform.tasks.vmware.ContextVmware;
import org.urbancode.terraform.tasks.vmware.PostCreateTask;


public class UDAgentPostCreateTask extends PostCreateTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(UDAgentPostCreateTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private ContextVmware context;

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

    }

    //----------------------------------------------------------------------------------------------
    public void configure() throws IOException, InterruptedException {
        String udDir = "/opt/urbandeploy/agent/bin/";
        Thread.sleep(5000);
        log.info("configuring agent");
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "udagent", "stop");
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sleep", "5");
        //String cfgCommand = "${ud.host} ${ud.port} ${ud.agent.name}";
        String cfgCommand = "${ud.host} ${ud.port} ${env.instance.name}";
        String resolvedCommand = context.resolve(cfgCommand);
        log.debug("resolved command: " + resolvedCommand);
        String[] split = resolvedCommand.split(" ");
        String agentName = split[2] + "-" + cloneTask.getInstanceName();
        log.debug("vmx path: " + environment.fetchVirtualHost().getVmxPath(vmToConfig));
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "configure-agent", split[0], split[1], agentName);
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sleep", "5");
        runCommand(vmUser, vmPassword, "runProgramInGuest", "/bin/sh", udDir + "udagent", "start");
    }
}

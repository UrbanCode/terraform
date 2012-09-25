package org.urbancode.terraform.tasks.microsoft.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.util.IOUtil;

public class AzureCmdRunner {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    Logger log = Logger.getLogger(AzureCmdRunner.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public AzureCmdRunner() {

    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String... args)
    throws IOException, InterruptedException{
        runCommand(Arrays.asList(args));
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(List<String> args)
    throws IOException, InterruptedException{
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("azure");
        commandLine.addAll(args);

        String cmd = "";
        for(String s : commandLine) {
            cmd = cmd + s + " ";
        }
        log.info("running Azure command: " + cmd);
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        InputStream procIn = process.getInputStream();
        String logAsString = IOUtils.toString(procIn);
        log.info(logAsString);
        IOUtil.getInstance().discardStream(procIn);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with code " + exitCode);
        }

    }
}

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.util.IOUtil;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.urbancode.x2o.tasks.ExtensionTask;
import com.vmware.vim25.mo.VirtualMachine;

public class PostCreateTask extends ExtensionTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(PostCreateTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    protected EnvironmentTaskVmware environment;
    protected CloneTask cloneTask;
    protected VirtualMachine vmToConfig;

    // defaults
    protected String vmUser = "root";
    protected String vmPassword = "password";

    protected String tempConfDir;
    protected String tempConfDirNoSeparator;

    //----------------------------------------------------------------------------------------------
    public PostCreateTask() {
    }

    //----------------------------------------------------------------------------------------------
    public PostCreateTask(CloneTask cloneTask) {
        this.cloneTask = cloneTask;
        this.environment = cloneTask.fetchEnvironment();
        this.vmToConfig = cloneTask.fetchVm();
        this.tempConfDirNoSeparator = System.getenv("TERRAFORM_HOME") +
                File.separator + "temp" + "-" + environment.fetchSuffix();
        this.tempConfDir = tempConfDirNoSeparator + File.separator;
    }

    //----------------------------------------------------------------------------------------------
    public void setValues(CloneTask cloneTask) {
        this.cloneTask = cloneTask;
        this.environment = cloneTask.fetchEnvironment();
        this.vmToConfig = cloneTask.fetchVm();
        this.tempConfDirNoSeparator = System.getenv("TERRAFORM_HOME") +
                File.separator + "temp" + "-" + environment.fetchSuffix();
        this.tempConfDir = tempConfDirNoSeparator + File.separator;
    }

    //----------------------------------------------------------------------------------------------
    public void setPassword(String password) {
        vmPassword = password;
    }

    //----------------------------------------------------------------------------------------------
    public void setUser(String user) {
        vmUser = user;
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String user, String password, String vmRunCommand, String... args)
    throws IOException, InterruptedException {
        runCommand(user, password, vmRunCommand, Arrays.asList(args));
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String vmUser, String vmPassword, String vmRunCommand, List<String> args)
    throws IOException, InterruptedException {
        if(vmUser == null || vmPassword == null) {
            log.error("Either VM user or password were null. " +
                    "They need to be specified in the template under the clone element.");
            throw new NullPointerException();
        }
        VirtualHost host = environment.fetchVirtualHost();
        host.waitForVmtools(vmToConfig);
        String vmx = host.getVmxPath(vmToConfig);
        String url = host.getUrl();
        String virtualHostUser = host.getUser();
        String virtualHostPassword = host.getPassword();
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("vmrun");
        commandLine.add("-T");
        commandLine.add("server");
        commandLine.add("-h");
        commandLine.add(url);
        commandLine.add("-u");
        commandLine.add(virtualHostUser);
        commandLine.add("-p");
        commandLine.add(virtualHostPassword);
        commandLine.add("-gu");
        commandLine.add(vmUser);
        commandLine.add("-gp");
        commandLine.add(vmPassword);
        commandLine.add(vmRunCommand);
        commandLine.add(vmx);
        commandLine.addAll(args);

        String cmd = "";
        for(String s : commandLine) {
            if(s.equals(vmPassword) || s.equals(virtualHostPassword)) {
                cmd = cmd + "**** ";
            }
            else {
                cmd = cmd + s + " ";
            }
        }
        cmd = cmd.trim();
        log.info("command line: " + cmd);

        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        InputStream procIn = process.getInputStream();
        IOUtil.getInstance().discardStream(procIn);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with code " + exitCode);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromGuestToHost(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(destination);
        String absDestination = temp.getAbsolutePath();
        args.add(origin);
        args.add(absDestination);
        runCommand(vmUser, vmPassword, "copyFileFromGuestToHost", args);
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromHostToGuest(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(origin);
        String absOrigin = temp.getAbsolutePath();
        args.add(absOrigin);
        args.add(destination);
        runCommand(vmUser, vmPassword, "copyFileFromHostToGuest", args);
    }

    //----------------------------------------------------------------------------------------------
    public void writeToFile(String fname, String text, boolean append)
    throws IOException {
        FileWriter out = new FileWriter(fname, append);
        out.write(text);
        out.close();
    }

    //----------------------------------------------------------------------------------------------
    public String join(String[] strArray, String delimiter) {
        String result = "";
        int noOfItems = 0;
        for (String item : strArray) {
            result += item;
            if (++noOfItems < strArray.length) {
                result += delimiter;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        File configDir = new File(this.tempConfDirNoSeparator);
        configDir.mkdirs();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
    }

}

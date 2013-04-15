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
package com.urbancode.terraform.tasks.aws;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.urbancode.terraform.tasks.aws.helpers.SshConnection;
import com.urbancode.terraform.tasks.aws.helpers.SshHelper;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.terraform.tasks.common.exceptions.PostCreateException;

public class SshTask extends PostCreateActionTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    final static private Logger log = Logger.getLogger(SshTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String cmds;
    private SshHelper sshHelper = new SshHelper();
    private SshConnection ssh = null;

    //----------------------------------------------------------------------------------------------
    public SshTask(TerraformContext context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    public void setCmds(String cmds) {
        this.cmds = cmds;
    }

    //----------------------------------------------------------------------------------------------
    public String getCmds() {
        return cmds;
    }

    //----------------------------------------------------------------------------------------------
    private SshConnection startSshConnection()
    throws PostCreateException, JSchException {
        SshConnection result = null;

        if (host == null || host.isEmpty()) {
            String msg = "No host specified to connect to";
            log.error(msg);
            throw new PostCreateException(msg);
        }

        if (user == null || user.isEmpty()) {
            String msg = "No user specified to connect as for instance " + host;
            log.error(msg);
            throw new PostCreateException(msg);
        }

        // are we connecting with a id-file?
        if (idFilePath != null && !idFilePath.isEmpty()) {
            File idFile = new File(idFilePath);
            if (idFile.exists() && idFile.isFile() && idFile.canRead()) {
                result = new SshConnection(host, user, idFile);
            }
            else {
                log.error("Unable to read file: " + idFile);
            }
        }
        // or are we connecting with a password?
        else if (pass != null && !pass.isEmpty()) {
            result = new SshConnection(host, user, pass);
        }
        // we can't connect without either an id-file or password
        else {
            log.error("No id file or password specifed. No way to connect to instance " + host);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void runCmds()
    throws JSchException, IOException, InterruptedException, ExecutionException {
        ChannelExec channel = ssh.run(cmds);
        channel.getOutputStream().close();

        Future<String> output = sshHelper.getOutputString(channel);
        Future<String> error = sshHelper.getErrorString(channel);

        String outputText = output.get();
        String errorText = error.get();

        while (true) {
            if (channel.isClosed()) {
                int exitCode = channel.getExitStatus();
                if (exitCode != 0) {
                    throw new IOException("Command failed with code " + exitCode + " : " + errorText);
                }
                break;
            }
            Thread.sleep(1000);
        }

        log.debug("Command out: " + outputText);
        log.debug("Command err: " + errorText);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create()
    throws PostCreateException {
        try {
            ssh = startSshConnection();
            // see if we made a connection
            if (ssh == null) {
                log.error("Attempted SSH connection: " + user + "@" + host + " || password: " + pass
                          + " || id-file: " + idFilePath);
                throw new PostCreateException("Could not make SSH connection!");
            }
            cmds = context.resolve(cmds);
            SshHelper.waitForPort(host, port);
            runCmds();
        }
        catch (RemoteException e) {
            log.error("Timeout while waiting for port: " + port + " on host: " + host, e);
            throw new PostCreateException(e);
        }
        catch (JSchException e) {
            log.error("JSch unable to make SshConnection on port: " + port + " on host: " + host, e);
            throw new PostCreateException(e);
        }
        catch (IOException e) {
            log.error("Failed commands! Tried to run: " + cmds, e);
            throw new PostCreateException(e);
        }
        catch (InterruptedException e) {
            log.error("Thread Inturrupted!", e);
            throw new PostCreateException(e);
        }
        catch (ExecutionException e) {
            throw new PostCreateException(e);
        }
        finally {
            if (ssh != null) {
                ssh.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        //not needed for this task
    }

}

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
package com.urbancode.terraform.tasks.aws.helpers;

import java.io.File;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshConnection {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(SshConnection.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private int maxTrials = 3;
    private String host;
    private String user;
    private String password;
    private String identity;
    private Session session;

    //----------------------------------------------------------------------------------------------
    public SshConnection(String host, String user, String password)
    throws JSchException {
        this.host = host;
        this.user = user;
        this.password = password;
        this.identity = null;
    }

    //----------------------------------------------------------------------------------------------
    public SshConnection(String host, String user, File identityFile)
    throws JSchException {
        this.host = host;
        this.user = user;

        try {
            this.identity = identityFile.getAbsolutePath();
        }
        catch (Exception e) {
            throw new JSchException("Password or valid identity file must be specified!", e);
        }

        this.password = null;
    }

    //----------------------------------------------------------------------------------------------
    public ChannelExec run(String command)
    throws JSchException {
        if (log.isDebugEnabled()) {
            log.debug("Command: " + command);
        }
        ChannelExec channel = null;
        int trial = 0;
        boolean connected = false;
        while (!connected) {
            try {
                if (session == null) {
                    connect();
                }
                channel = (ChannelExec) session.openChannel("exec");
                channel.setPty(false);
                channel.setCommand(command);
                channel.connect();
                connected = true;
            }
            catch (JSchException e) {
                close();

                if (trial < maxTrials) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    trial++;
                }
                else {
                    throw e;
                }
            }
        }
        assert channel != null;
        return channel;
    }

    //----------------------------------------------------------------------------------------------
    public void close() {
        Session session = this.session;
        this.session = null;
        if (session != null) {
            try {
                session.disconnect();
            }
            catch (Exception swallow) {
            }
        }
    }

    private void connect()
    throws JSchException {
        JSch jsch = new JSch();
        if (log.isDebugEnabled()) {
            log.debug("Connecting to " + host + " as " + user);
        }
        if (identity != null) {
            jsch.addIdentity(identity);
        }
        session = jsch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");

        if (password != null) {
            session.setPassword(password);
        }

        session.connect(60000);
    }
}

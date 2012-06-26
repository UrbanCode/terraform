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
package org.urbancode.terraform.tasks.aws.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jcraft.jsch.ChannelExec;
import com.urbancode.commons.util.IO;
import com.urbancode.commons.util.concurrent.NamedThreadFactory;
import com.urbancode.commons.util.concurrent.NamedThreadFactory.ThreadMode;


public class SshHelper {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    private String name = "SshHelper";
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    static public boolean isPortActive(String host, int port) {
        Socket s = null;
        try {
            s = new Socket();
            s.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(host, port);
            s.connect(sa, 3000);
            return true;
        } 
        catch (IOException e) { } 
        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
    
    static public void waitForPort(String ip, int port) 
    throws RemoteException {
        long pollInterval = 3000L;
        long timeoutInterval = 10L * 60L * 1000L;
        long start = System.currentTimeMillis();
        
        while(!isPortActive(ip, port)) {
            try {
                Thread.sleep(pollInterval);
            }
            catch (Exception e) {
                //swallow
            }
            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException("Timeout waiting for SSH port!");
            }
        }
        return;
    }

    //----------------------------------------------------------------------------------------------
    static public void waitForSshPort(String ip) 
    throws RemoteException {
        waitForPort(ip, 22);
    }
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private ExecutorService threadPool = Executors.newCachedThreadPool(
        new NamedThreadFactory("ssh-helper", ThreadMode.DAEMON));

    //----------------------------------------------------------------------------------------------
    public Future<String> getErrorString(final ChannelExec channel) {
        return threadPool.submit(new Callable<String>() {
            @Override
            public String call()
            throws Exception {
                StringBuilder builder = new StringBuilder();
                Reader in = IO.reader(channel.getErrStream(), IO.utf8());
                try {
                    IO.copy(in, builder);
                }
                finally {
                    in.close();
                }
                return builder.toString();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    public Future<String> getOutputString(final ChannelExec channel) {
        return threadPool.submit(new Callable<String>() {
            @Override
            public String call()
            throws Exception {
                StringBuilder builder = new StringBuilder();
                Reader in = IO.reader(channel.getInputStream(), IO.utf8());
                try {
                    IO.copy(in, builder);
                }
                finally {
                    in.close();
                }
                return builder.toString();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    public Future<?> sendInputString(final ChannelExec channel, final String input) {
        return threadPool.submit(new Callable<Void>() {
            @Override
            public Void call()
            throws Exception {
                Writer out = IO.writer(channel.getOutputStream(), IO.utf8());
                try {
                    IO.copy(IO.reader(input), out);
                }
                finally {
                    out.close();
                }
                return null;
            }
        });
    }

}

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jcraft.jsch.ChannelExec;
//import com.urbancode.commons.util.IO;
//import com.urbancode.commons.util.concurrent.NamedThreadFactory;
//import com.urbancode.commons.util.concurrent.NamedThreadFactory.ThreadMode;


public class SshHelper {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    private String name = "SshHelper";
    
    // TODO - change prop name
    final static private int BUFFER_SIZE =
            Integer.getInteger("com.urbancode.commons.util.IO.bufferSize", 8192);
    
    //----------------------------------------------------------------------------------------------
    static public void copy(Reader in, Appendable appendable)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        CharBuffer charBuffer = CharBuffer.wrap(buffer);
        int count;
        while ((count = in.read(buffer)) != -1) {
            appendable.append(charBuffer, 0, count);
        }
    }
    
    //----------------------------------------------------------------------------------------------
    static public void copy(Reader in, Writer out)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }
    
    static public OutputStreamWriter writer(OutputStream out, String charset) {
        OutputStreamWriter result;
        if (charset == null) {
            result = writer(out);
        }
        else {
            result = writer(out, Charset.forName(charset));
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out, CharsetEncoder encoder) {
        OutputStreamWriter result;
        if (encoder == null) {
            result = writer(out);
        }
        else {
            result = new OutputStreamWriter(out, encoder);
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out, Charset charset) {
        OutputStreamWriter result;
        if (charset == null) {
            result = writer(out);
        }
        else {
            CharsetEncoder encoder;
            encoder = charset.newEncoder();
            result = writer(out, encoder);
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out) {
        OutputStreamWriter result;
        
        result = new OutputStreamWriter(out);
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in, String charsetName) {
        InputStreamReader result;
        if (charsetName == null) {
            result = reader(in);
        }
        else {
            result = new InputStreamReader(in, Charset.forName(charsetName));
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    static public StringReader reader(String string) {
        return new StringReader(string);
    }
    
    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in) {
        InputStreamReader result;
        
        result = new InputStreamReader(in);
            
        return result;
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
    final private ExecutorService threadPool = Executors.newCachedThreadPool();
            //Executors.newCachedThreadPool(new NamedThreadFactory("ssh-helper", ThreadMode.DAEMON));
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public Future<String> getErrorString(final ChannelExec channel) {
        return threadPool.submit(new Callable<String>() {
            @Override
            public String call()
            throws Exception {
                StringBuilder builder = new StringBuilder();
                Reader in = reader(channel.getErrStream(), "UTF-8");
                try {
                    copy(in, builder);
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
                Reader in = reader(channel.getInputStream(), "UTF-8");
                try {
                    copy(in, builder);
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
                Writer out = writer(channel.getOutputStream(), "UTF-8");
                try {
                    copy(reader(input), out);
                }
                finally {
                    out.close();
                }
                return null;
            }
        });
    }

}

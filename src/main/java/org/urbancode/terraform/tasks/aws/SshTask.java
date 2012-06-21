package org.urbancode.terraform.tasks.aws;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.PostCreateException;
import org.urbancode.terraform.tasks.aws.helpers.SshConnection;
import org.urbancode.terraform.tasks.aws.helpers.SshHelper;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.SubTask;
import org.urbancode.terraform.tasks.util.PropertyResolver;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class SshTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    final static private Logger log = Logger.getLogger(SshTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String host;
    private String idFilePath;
    
    private int port = 22;  // default ssh port
    private String user;
    private String pass;
    
    private String cmds;
//    private List<String> commandElements = new ArrayList<String>();
    
    //----------------------------------------------------------------------------------------------
    public SshTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public void setUser(String user) {
        this.user = user;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPassword(String pass) {
        this.pass = pass;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setHost(String host) {
        this.host = host;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setPort(int port) {
        this.port = port;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setIdFilePath(String idFilePath) {
        this.idFilePath = idFilePath;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCmds(String cmds) {
        this.cmds = cmds;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return pass;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getHost() {
        return host;
    }
    
    //----------------------------------------------------------------------------------------------
    public int getPort() {
        return port;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getIdFilePath() {
        return idFilePath;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getCmds() {
        return cmds;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws PostCreateException {
        SshConnection ssh = null;
        SshHelper sshHelper = new SshHelper();
        
        try {
        if (idFilePath != null && !idFilePath.isEmpty()) {
            File idFile = new File(idFilePath);
            if (idFile.exists() && idFile.isFile() && idFile.canRead()) {
                ssh = new SshConnection(host, user, idFile);
            }
            else {
                log.error("Unable to read file: " + idFile);
            }
        }
        else if (pass != null && !pass.isEmpty()) {
            ssh = new SshConnection(host, user, pass);
        }
        else {
            log.error("No id file or password specifed. No way to connect to instance " + host);
        }
        
        // resolve any props in the cmds
        cmds = context.resolve(cmds);
        
        SshHelper.waitForPort(host, port); 
        
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
        // TODO Auto-generated method stub
        
    }

}

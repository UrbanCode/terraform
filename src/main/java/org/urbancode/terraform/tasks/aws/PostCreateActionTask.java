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
package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.TerraformContext;

import com.urbancode.x2o.tasks.SubTask;

/**
 * This class holds all info for creating a connection to a server which will be necessary if you'd
 * like to run some actions on it after it has started.
 *
 * @author ncc
 *
 */
public abstract class PostCreateActionTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PostCreateActionTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    protected String host;
    protected String idFilePath;

    protected int port = 22;  // default ssh port
    protected String user;
    protected String pass;

    public PostCreateActionTask(TerraformContext context) {
        super(context);
    }

    public PostCreateActionTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param user - The user to login as and run the Post Create Actions
     */
    public void setUser(String user) {
        this.user = user;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * This is not necessary to set if an ID file is specified.
     * This sets the password to login with for the user defined.
     * @param pass - The password associated with the user to login with
     */
    public void setPassword(String pass) {
        this.pass = pass;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param host - The IP / hostname of the machine to connect to.
     */
    public void setHost(String host) {
        this.host = host;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param port - The port to connect on
     */
    public void setPort(int port) {
        this.port = port;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * This is not needed if a connection is being made with a password. If both the ID file and the
     * password are set, the ID file takes priority. For Amazon Web Services, this would most likely
     * be the .pem file the instance was launched with.
     * @param idFilePath - the absolute path to the id file to connect with
     */
    public void setIdFilePath(String idFilePath) {
        this.idFilePath = idFilePath;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the user to connect to the host as
     */
    public String getUser() {
        return user;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the password for the user
     */
    public String getPassword() {
        return pass;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the host to connect to
     */
    public String getHost() {
        return host;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the port to connect on
     */
    public int getPort() {
        return port;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the path to the ID file
     */
    public String getIdFilePath() {
        return idFilePath;
    }

}

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

import com.urbancode.x2o.tasks.SubTask;


public class HealthCheckTask extends SubTask {


    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(HealthCheckTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String protocol;
    private String path;

    private int port;
    private int interval;
    private int timeout;
    private int unhealthy;
    private int healthy;

    //----------------------------------------------------------------------------------------------
    public HealthCheckTask(ContextAWS context) {
        this.context = context;
    }

    //----------------------------------------------------------------------------------------------
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    //----------------------------------------------------------------------------------------------
    public void setPath(String path) {
        this.path = path;
    }

    //----------------------------------------------------------------------------------------------
    public void setPort(int port) {
        this.port = port;
    }

    //----------------------------------------------------------------------------------------------
    public void setInterval(int interval) {
        this.interval = interval;
    }

    //----------------------------------------------------------------------------------------------
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    //----------------------------------------------------------------------------------------------
    public void setUnhealthyCount(int unhealthy) {
        this.unhealthy = unhealthy;
    }

    //----------------------------------------------------------------------------------------------
    public void setHealthyCount(int healthy) {
        this.healthy = healthy;
    }

    //----------------------------------------------------------------------------------------------
    public String getProtocol() {
        return protocol;
    }

    //----------------------------------------------------------------------------------------------
    public String getPath() {
        return path;
    }

    //----------------------------------------------------------------------------------------------
    public int getPort() {
        return port;
    }

    //----------------------------------------------------------------------------------------------
    public int getInterval() {
        return interval;
    }

    //----------------------------------------------------------------------------------------------
    public int getTimeout() {
        return timeout;
    }

    //----------------------------------------------------------------------------------------------
    public int getUnhealthyCount() {
        return unhealthy;
    }

    //----------------------------------------------------------------------------------------------
    public int getHealthyCount() {
        return healthy;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {

    }

}

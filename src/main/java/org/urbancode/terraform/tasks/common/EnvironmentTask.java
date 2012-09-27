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
package org.urbancode.terraform.tasks.common;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.EnvironmentRestorationException;


public class EnvironmentTask extends Task {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    final static private Logger log = Logger.getLogger(EnvironmentTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    protected String name;
    protected String prefix;
    protected String uuid = null;
    protected long startTime;

    //----------------------------------------------------------------------------------------------
    /**
     *
     */
    public EnvironmentTask() {
        super(null);
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param context - The Context that the environment is in
     */
    public EnvironmentTask(Context context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return The name of the environment.
     */
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public String fetchPrefix() {
        return prefix;
    }

    //----------------------------------------------------------------------------------------------
    public String fetchUUID() {
        return uuid;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return The time (in milliseconds) that the environment was started.
     */
    public long getStartTime() {
        return startTime;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param startTime - The time (in milliseconds) that the environment was started
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @param name - The name of the environment
     */
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * this is sort of a temporary solution right now to ensure deletion of temporary folders
     * @param uuid - a unique string (we usually expect this to be 4 hexadecimal characters, eg. 8fe0)
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Appends a unique string to the environment name so there are not file name conflicts
     * @param uuid
     */
    public void addUUIDToEnvName(String uuid) {
        this.uuid = uuid;
        log.debug("Set environment (" + name + ") uuid to " + this.uuid);

        if (name != null) {
            prefix = this.name;
        }

        if (uuid != null) {
            name = (name + "-" + this.uuid);
        }
        log.debug("Environment full name: " + name);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates the defined environment and all sub-objects it includes.
     */
    @Override
    public void create()
    throws EnvironmentCreationException {
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Restore the defined environment and all sub-objects it includes.
     */
    @Override
    public void restore()
    throws EnvironmentRestorationException {
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Destroys the whole environment, including all sub-objects.
     */
    @Override
    public void destroy()
    throws EnvironmentDestructionException {
    }
}

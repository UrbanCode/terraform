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

import com.urbancode.x2o.tasks.Task;


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
    protected String suffix = null;
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
    public EnvironmentTask(TerraformContext context) {
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
    public String fetchSuffix() {
        return suffix;
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
     * @param suffix - a unique string (we expect this to be 4 base 62 characters, eg. 8ZeJ)
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Appends a unique string to the environment name so there are not file name conflicts
     * @param suffix
     */
    public void addSuffixToEnvName(String suffix) {
        this.suffix = suffix;
        log.debug("Set environment (" + name + ") uuid to " + this.suffix);

        if (name != null) {
            prefix = this.name;
        }

        if (suffix != null) {
            name = (name + "-" + this.suffix);
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

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

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.EnvironmentRestorationException;
import org.urbancode.terraform.tasks.util.PropertyResolver;


public interface Context {

    //----------------------------------------------------------------------------------------------
    public void create()
    throws EnvironmentCreationException;

    //----------------------------------------------------------------------------------------------
    public void restore()
    throws EnvironmentRestorationException;

    //----------------------------------------------------------------------------------------------
    public void destroy()
    throws EnvironmentDestructionException;

    //----------------------------------------------------------------------------------------------
    public void setResolver(PropertyResolver resolver);

    //----------------------------------------------------------------------------------------------
    public void setCredentials(Credentials credentials)
    throws CredentialsException;

    //----------------------------------------------------------------------------------------------
    public EnvironmentTask getEnvironment();

    //----------------------------------------------------------------------------------------------
    public Credentials fetchCredentials();

    //----------------------------------------------------------------------------------------------
    public void setProperty(String prop, String value);

    //----------------------------------------------------------------------------------------------
    public String resolve(String resolve);
}

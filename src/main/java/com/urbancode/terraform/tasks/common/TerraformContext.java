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
package com.urbancode.terraform.tasks.common;


import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsException;
import com.urbancode.x2o.tasks.Context;
import com.urbancode.x2o.util.PropertyResolver;


public abstract class TerraformContext implements Context {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    protected PropertyResolver resolver;

    //----------------------------------------------------------------------------------------------
    @Override
    public void setResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void setProperty(String propName, String propValue) {
        resolver.setProperty(propName, propValue);
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public String resolve(String propName) {
        return resolver.resolve(propName);
    }

    //----------------------------------------------------------------------------------------------
    public abstract void setCredentials(Credentials credentials)
    throws CredentialsException;

    //----------------------------------------------------------------------------------------------
    public abstract EnvironmentTask getEnvironment();

    //----------------------------------------------------------------------------------------------
    public abstract Credentials fetchCredentials();

}

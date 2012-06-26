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
package org.urbancode.terraform.tasks.vmware.util;

import java.io.Serializable;

public class VmHost implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private String url;
    private String user;
    private String password;

    //----------------------------------------------------------------------------------------------
    public VmHost(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return url;
    }

    //----------------------------------------------------------------------------------------------
    public String getUser() {
        return user;
    }

    //----------------------------------------------------------------------------------------------
    public String getPassword() {
        return password;
    }
}

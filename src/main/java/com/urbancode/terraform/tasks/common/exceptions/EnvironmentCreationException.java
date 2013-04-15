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
package com.urbancode.terraform.tasks.common.exceptions;

import com.urbancode.x2o.tasks.CreationException;

public class EnvironmentCreationException extends CreationException {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException() {
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentCreationException(Throwable cause) {
        super(cause);
    }
}

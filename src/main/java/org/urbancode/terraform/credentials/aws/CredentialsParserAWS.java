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
package org.urbancode.terraform.credentials.aws;

import java.util.Properties;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;


public class CredentialsParserAWS extends CredentialsParser {

    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials parse(Properties props) {
        Credentials result = null;
        
        String name = props.getProperty("name");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        
        String accessKey = props.getProperty("access.key");
        String secretKey = props.getProperty("secret.key");
        
        result = new CredentialsAWS(name, username, password, accessKey, secretKey);
        
        return result;
    }

}

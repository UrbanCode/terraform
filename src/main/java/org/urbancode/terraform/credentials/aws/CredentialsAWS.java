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

import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.common.Credentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class CredentialsAWS extends Credentials {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(CredentialsAWS.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private String accessKey;
    private String secretKey;
    private AWSCredentials awsCreds;
    
    //----------------------------------------------------------------------------------------------
    public CredentialsAWS(String name, String username, String password, String accessKey, String secretKey) {
        super(name, username, password);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        awsCreds = null;
    }
    //----------------------------------------------------------------------------------------------
    public String getAccessKey() {
        return accessKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSecretKey() {
        return secretKey;
    }
    
    //----------------------------------------------------------------------------------------------
    public AWSCredentials getBasicAWSCredentials() 
    throws NullPointerException {
        awsCreds = createBasicAWSCredentials();
        
        return awsCreds;
    }
    
    //----------------------------------------------------------------------------------------------
    private AWSCredentials createBasicAWSCredentials() 
    throws NullPointerException {
        AWSCredentials result;
        
        if (accessKey == null || "".equals(accessKey)) {
            log.error("access key for Credentials " + getName() + " is empty!");
            throw new NullPointerException("Empty Access Key");
        }
        
        if (secretKey == null || "".equals(secretKey)) {
            log.error("secret key for Credentials " + getName() + " is empty!");
            throw new NullPointerException("Empty Secret Key");
        }
        
        result = new BasicAWSCredentials(accessKey, secretKey);
        return result;
    }
}

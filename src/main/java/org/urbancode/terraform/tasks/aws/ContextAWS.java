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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.credentials.aws.CredentialsAWS;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.util.PropertyResolver;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

public class ContextAWS implements Context {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ContextAWS.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
   
    private AmazonEC2 ec2Client;
    private AmazonElasticLoadBalancing elbClient;
    private AWSHelper helper;
   
    private CredentialsAWS credentials;
    private EnvironmentTaskAWS environment;
   
    private PropertyResolver resolver;
   
    private Map<String,String> keyPairs = new HashMap<String,String>();
    private Map<String,String> instanceSizes = new HashMap<String,String>();
    
    //----------------------------------------------------------------------------------------------
    public ContextAWS() 
    throws Exception {
        environment = null;
        initSizeMap();
        helper = new AWSHelper();
    }
    
    private void initSizeMap() {
        instanceSizes.put("micro", "t1.micro");
        instanceSizes.put("small", "m1.small");
        instanceSizes.put("medium", "m1.medium");
        instanceSizes.put("large", "m1.large");
        instanceSizes.put("xlarge", "m1.xlarge");
        instanceSizes.put("medium-cpu", "c1.medium");
        instanceSizes.put("xlarge-cpu", "c1.xlarge");
        instanceSizes.put("xlarge-mem", "m2.xlarge");
        instanceSizes.put("2xlarge-mem", "m2.2xlarge");
        instanceSizes.put("4xlarge-mem", "m2.4xlarge");
    }
    
    //----------------------------------------------------------------------------------------------
    public void setProperty(String name, String value) {
        resolver.setProperty(name, value);
    }
    
    //----------------------------------------------------------------------------------------------
    protected String getKeyByName(String reference) { 
        return keyPairs.get(reference);
    }

    //----------------------------------------------------------------------------------------------
    protected String getSizeByName(String reference) {
        return instanceSizes.get(reference);
    }
    
    //----------------------------------------------------------------------------------------------
    protected AWSCredentials getBasicAwsCreds() {
        AWSCredentials result = null;
        
        if (credentials != null) {
            result = credentials.getBasicAWSCredentials();
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    protected AmazonEC2 getEC2Client() {
        if (ec2Client == null) {
            ec2Client = new AmazonEC2Client(getBasicAwsCreds());
        }
        return ec2Client;
    }
    
    //----------------------------------------------------------------------------------------------
    protected AmazonElasticLoadBalancing getELBClient() {
        if (elbClient == null) {
            elbClient = new AmazonElasticLoadBalancingClient(getBasicAwsCreds());
        }
        return elbClient;
    }
    
    //----------------------------------------------------------------------------------------------
    protected AWSHelper getAWSHelper() {
        return helper;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public EnvironmentTask getEnvironment() {
        return environment;
    }
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskAWS createEnvironment() {
        environment = new EnvironmentTaskAWS(this);
        return environment;
    }
    
    //----------------------------------------------------------------------------------------------
    public String resolve(String toResolve) {
        return resolver.resolve(toResolve);
    }
    
    //----------------------------------------------------------------------------------------------
    public void keyFileMap(String newKeyName, String realKeyName) {
        keyPairs.put(newKeyName, realKeyName);
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        log.debug("ContextAWS: create()");
        try {
            environment.create();
        }
        catch (EnvironmentCreationException e) {
            log.fatal("Did not finish starting environment!", e);
            throw new RuntimeException(e);
        }
        finally {
            if (ec2Client != null) {
                ec2Client.shutdown();
                ec2Client = null;
                log.info("Closed connection to AWS:EC2");
            }
            if (elbClient != null) {
                elbClient.shutdown();
                elbClient = null;
                log.info("Closed conenction to AWS:ELB");
            }
        }
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        try {
            environment.destroy();
        } catch (EnvironmentDestructionException e) {
            log.fatal("Could not completely destroy environment!", e);
            throw new RuntimeException(e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials fetchCredentials() {
        return credentials;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCredentials(Credentials credentials) 
    throws CredentialsException {
        if (credentials instanceof CredentialsAWS) {
            this.credentials = (CredentialsAWS) credentials;
        }
        else {
            log.error("Credentials is not of type " + CredentialsAWS.class.getName());
            throw new CredentialsException("Bad credential type");
        }
    }
}


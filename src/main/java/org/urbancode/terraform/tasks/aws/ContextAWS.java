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
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.credentials.aws.CredentialsAWS;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.common.TerraformContext;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.common.exceptions.EnvironmentRestorationException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.urbancode.x2o.tasks.CreationException;
import com.urbancode.x2o.util.PropertyResolver;

public class ContextAWS implements TerraformContext {

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

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @throws Exception
     */
    public ContextAWS() {
        environment = null;
        helper = new AWSHelper();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setProperty(String name, String value) {
        resolver.setProperty(name, value);
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return Basic AWS Credentials used to make connection to Amazon Web Services (e.g. EC2)
     */
    protected AWSCredentials getBasicAwsCreds() {
        AWSCredentials result = null;

        if (credentials != null) {
            result = credentials.getBasicAWSCredentials();
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return an AmazonEC2 which is an active connection to Amazon EC2
     */
    public AmazonEC2 fetchEC2Client() {
        if (ec2Client == null) {
            ec2Client = new AmazonEC2Client(getBasicAwsCreds());
        }
        return ec2Client;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return an AmazonElasticLoadBalancing which is an active connection to Amazon Elastic Load
     * Balancing
     */
    public AmazonElasticLoadBalancing fetchELBClient() {
        if (elbClient == null) {
            elbClient = new AmazonElasticLoadBalancingClient(getBasicAwsCreds());
        }
        return elbClient;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return AWS helper class used for making calls to Amazon
     */
    protected AWSHelper getAWSHelper() {
        return helper;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *
     * @return the environment
     */
    @Override
    public EnvironmentTask getEnvironment() {
        return environment;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates and return a new EnvironmentTaskAWS
     * @return a new EnvironmentTask with this as its Context
     */
    public EnvironmentTaskAWS createEnvironment() {
        environment = new EnvironmentTaskAWS(this);
        return environment;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * A property is declared with ${}  (e.g.  ${server.name} )
     *
     * @param toResolve - The string to be ran through the resolver
     *
     * @return the same string as the input, but with all properties resolved
     */
    @Override
    public String resolve(String toResolve) {
        return resolver.resolve(toResolve);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param resolver - The property resolver to use for this Context
     */
    @Override
    public void setResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }

    //----------------------------------------------------------------------------------------------
    /**
     *  @return the Credentials file that this context holds - used to make connections to the
     *  service provider
     */
    @Override
    public Credentials fetchCredentials() {
        return credentials;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param credentials - The credentials to use for creating the environment. Should be of type
     * CredentialsAWS because we need the Amazon connection info.
     */
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

    //----------------------------------------------------------------------------------------------
    /**
     * Creates the whole environment and shutsdown connection afterwards
     */
    @Override
    public void create()
    throws CreationException {
        log.debug("ContextAWS: create()");
        try {
            if (environment != null) {
                environment.create();
            }
            else {
                throw new NullPointerException("No environment");
            }
        }
        catch (EnvironmentCreationException e) {
            log.fatal("Did not finish starting environment!", e);
            throw e;
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
    /**
     * Destroys the whole environment and shutsdown connections afterwards
     */
    @Override
    public void destroy()
    throws EnvironmentDestructionException {
        try {
            if (environment != null) {
                environment.destroy();
            }
            else {
                throw new NullPointerException("No environment");
            }
        } catch (EnvironmentDestructionException e) {
            log.fatal("Could not completely destroy environment!", e);
            throw e;
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
    public void restore() throws EnvironmentRestorationException {
        try {
            if (environment != null) {
                environment.restore();
            }
            else {
                throw new NullPointerException("No environment");
            }
        } catch (EnvironmentRestorationException e) {
            log.fatal("Could not completely destroy environment!", e);
            throw e;
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
}


package com.urbancode.terraform.tasks.vcloud;

import org.apache.log4j.Logger;

import com.savvis.sdk.api.client.ApiClient;
import com.savvis.sdk.oauth.client.OAuthClient;
import com.savvis.sdk.oauth.client.OAuthClientRequest;
import com.savvis.sdk.oauth.common.OAuthCredentials;
import com.savvis.sdk.oauth.connections.HttpApiResponse;
import com.urbancode.terraform.credentials.common.CredentialsException;
import com.urbancode.terraform.credentials.vcloud.CredentialsVCloud;

public class SavvisClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(SavvisClient.class);
    
    static private SavvisClient instance = new SavvisClient();
    
    static public final String GET_METHOD = "GET";
    static public final String POST_METHOD = "POST";
    static public final String PUT_METHOD = "PUT";
    static public final String DELETE_METHOD = "DELETE";
    
    static public SavvisClient getInstance() {
        return instance;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    String organizationName;
    OAuthCredentials oAuthCreds;
    
    //----------------------------------------------------------------------------------------------
    public OAuthCredentials getCredentials() {
        return oAuthCreds;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getOrganizationName() {
        return organizationName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCredentials(CredentialsVCloud creds) 
    throws CredentialsException {
        this.oAuthCreds = retrieveOAuthCreds(creds);
        this.organizationName = creds.getOrganization();
    }
    
    //----------------------------------------------------------------------------------------------
    private OAuthCredentials retrieveOAuthCreds(CredentialsVCloud creds) 
    throws CredentialsException {
        
        OAuthClientRequest oAuthRequest = new OAuthClientRequest();
        oAuthRequest.setApiKey(creds.getApiKey());
        oAuthRequest.setApiSecret(creds.getSecretKey());
        oAuthRequest.setUsername(creds.getUser());
        oAuthRequest.setPassword(creds.getPassword());
        oAuthRequest.setOrgName(creds.getOrganization());
        oAuthRequest.setVdcLocation(creds.getLocation());

        OAuthCredentials oauthCredentials = null;
        try {
            oauthCredentials = OAuthClient.getInstance().getOAuthToken(oAuthRequest);
        }
        catch (Exception e) {
            log.error("Failed to authenticate with supplied vCloud credentials", e);
            throw new CredentialsException(e);
        }
        log.debug("Successfully authenticated to vCloud");

        return oauthCredentials;
    }
    
    //----------------------------------------------------------------------------------------------
    public HttpApiResponse makeApiCallWithSuffix(String urlSuffix, String method, String body, String requestContentType) 
    throws Exception {
        String url = oAuthCreds.getApiBaseLocation() + urlSuffix;
        
        return makeApiCall(url, method, body, requestContentType);
    }
    
    //----------------------------------------------------------------------------------------------
    public HttpApiResponse makeApiCall(String url, String method, String body, String requestContentType) 
    throws Exception {
        ApiClient apiClient = new ApiClient(oAuthCreds);
        HttpApiResponse httpApiResponse = new HttpApiResponse();
        log.debug("Sending request to: " + url);
        try {
            httpApiResponse = apiClient.getApiResponse(url, method, requestContentType, null, body);
            validateStatusCode(httpApiResponse);
        }
        catch (Exception e) {
            log.error("Exception while making API call to " + url, e);
            throw e;
        }
        
        return httpApiResponse;
    }
    
    //----------------------------------------------------------------------------------------------
    private void validateStatusCode(HttpApiResponse response) throws BadResponse {
        int status = response.getStatusCode();
        log.debug("status code: " + status);
        if (status > 299) {
            throw new BadResponse("The previous HTTP call returned status: " + status + " with message:" +
                    response.getResponseString(), status);
        }
    }
}

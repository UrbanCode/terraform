package com.urbancode.terraform.tasks.vcloud;

import org.apache.log4j.Logger;

import com.savvis.sdk.api.client.ApiClient;
import com.savvis.sdk.oauth.client.OAuthClient;
import com.savvis.sdk.oauth.client.OAuthClientRequest;
import com.savvis.sdk.oauth.common.OAuthCredentials;
import com.savvis.sdk.oauth.connections.HttpApiResponse;
import com.urbancode.terraform.credentials.vcloud.CredentialsVCloud;

public class SavvisClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(SavvisClient.class);
    static private final String GET_METHOD = "GET";
    static private final String POST_METHOD = "POST";
    static private final String PUT_METHOD = "PUT";
    static private final String DELETE_METHOD = "DELETE";

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    OAuthCredentials oAuthCreds;
    
    //----------------------------------------------------------------------------------------------
    public SavvisClient(CredentialsVCloud creds) {
        this.oAuthCreds = getOAuthCreds(creds);
    }
    
    public OAuthCredentials getOAuthCreds(CredentialsVCloud creds){
        
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
            //TODO exception handling
        }
        log.debug("Successfully authenticated to vCloud");

        return oauthCredentials;
    }
    
    public HttpApiResponse makeApiCall(String urlSuffix, String method, String body, String requestContentType) {
        ApiClient apiClient = new ApiClient(oAuthCreds);
        HttpApiResponse httpApiResponse = new HttpApiResponse();
        String url = oAuthCreds.getApiBaseLocation() + urlSuffix;
        
        try {
            httpApiResponse = apiClient.getApiResponse(url, method, requestContentType, null, body);
            validateStatusCode(httpApiResponse);
        } catch (Exception e) {
            log.error("Exception while making API call to " + url, e);
        }
        
        return httpApiResponse;
    }
    
    private void validateStatusCode(HttpApiResponse response) throws Exception {
        int status = response.getStatusCode();
        log.debug("status code: " + status);
        if (status > 299) {
            //TODO custom exception type
            throw new Exception("The previous HTTP call returned status: " + status + " with message:" +
                    response.getResponseString());
        }
    }
}

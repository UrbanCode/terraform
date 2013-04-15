package org.urbancode.terraform.tasks.rackspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ConnectException;
import java.net.URI;
import java.nio.CharBuffer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


public class RackspaceRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(RackspaceRestClient.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private HttpClient httpClient;
    private String authToken = null;
    private String tenantID = null;

    //----------------------------------------------------------------------------------------------
    protected RackspaceRestClient() {
        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClient(connectionManager);
    }

    //----------------------------------------------------------------------------------------------
    protected String getAuthToken() {
        return authToken;
    }

    //----------------------------------------------------------------------------------------------
    protected String getTenantID() {
        return tenantID;
    }

    //----------------------------------------------------------------------------------------------
    public void authenticate(String username, String apiKey) throws AuthenticationException {
        try {
            JSONObject data = new JSONObject();
            JSONObject auth = new JSONObject();
            JSONObject usernameApiKey = new JSONObject();
            usernameApiKey.put("username", username)
            .put("apiKey", apiKey);
            auth.put("RAX-KSKEY:apiKeyCredentials", usernameApiKey);
            data.put("auth", auth);

            String uri = "https://identity.api.rackspacecloud.com/v2.0/tokens";
            PostMethod method = new PostMethod(uri);
            RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", null);
            method.setRequestEntity(entity);

            int status = invokeMethod(method);
            if (200 <= status && status <= 202) {
                String body = getBody(method);
                JSONObject tokenJSON = new JSONObject(body).getJSONObject("access").getJSONObject("token");
                authToken = tokenJSON.getString("id");
                tenantID = tokenJSON.getJSONObject("tenant").getString("id");
                log.debug("auth token: " + authToken);
                log.debug("tenant ID: " + tenantID);
            }
            else {
                log.debug("Exception when authenticating.");
                throw new IOException(String.format("%d %s",
                    status,
                    HttpStatus.getStatusText(status)));
            }
        }
        catch(IOException e0) {
            throw new AuthenticationException(e0);
        }
        catch(JSONException e1) {
            throw new AuthenticationException(e1);
        }

    }

    //----------------------------------------------------------------------------------------------
    protected String getBody(HttpMethodBase method)
    throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStream body = method.getResponseBodyAsStream();
        if (body != null) {;
            Reader reader = new InputStreamReader(body, "UTF-8");
            try {
                copy(reader, builder);
            }
            finally {
                reader.close();
            }
        }
        return builder.toString();
    }

    //----------------------------------------------------------------------------------------------
    protected void discardBody(HttpMethodBase method)
    throws IOException {
        InputStream body = method.getResponseBodyAsStream();
        if (body != null) {
            OutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(body, out);
            out.close();
        }
    }

    //----------------------------------------------------------------------------------------------
    protected int invokeMethod(HttpMethodBase method)
    throws IOException {
        int result;

        try {
            result = httpClient.executeMethod(method);
        }
        catch (ConnectException e) {
            throw (ConnectException) new ConnectException(
                "Failed connecting to " + method.getURI() + ": " + e.getMessage()).
                initCause(e);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    protected String encodePath(String path) {
        String result;
        URI uri;
        try {
            uri = new URI(null,null, path, null);
            result = uri.toASCIIString();
        }
        catch (Exception e) {
            result = path;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void copy(Reader in, Appendable appendable)
    throws IOException {
        char[] buffer = new char[8192];
        CharBuffer charBuffer = CharBuffer.wrap(buffer);
        int count;
        while ((count = in.read(buffer)) != -1) {
            appendable.append(charBuffer, 0, count);
        }
    }
}

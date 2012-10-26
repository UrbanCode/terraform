package org.urbancode.terraform.tasks.rackspace;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.x2o.tasks.SubTask;

public class ServerTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(ServerTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskRackspace env;
    private String name;
    private String region;
    private String image;
    private String flavor;
    private String id;

    //----------------------------------------------------------------------------------------------
    public ServerTask(EnvironmentTaskRackspace env) {
        this.env = env;
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public String getRegion() {
        return region;
    }

    //----------------------------------------------------------------------------------------------
    public String getImage() {
        return image;
    }

    //----------------------------------------------------------------------------------------------
    public String getFlavor() {
        return flavor;
    }

    //----------------------------------------------------------------------------------------------
    public String getId() {
        return id;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setRegion(String region) {
        if(Region.containsIgnoreCase(region)) {
            this.region = region.toLowerCase();
        }
        else {
            log.warn("You did not enter a valid region for a server.");
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setImage(String image) {
        this.image = image;
    }

    //----------------------------------------------------------------------------------------------
    public void setFlavor(String flavor) {
        if (Flavor.contains(flavor)) {
            this.flavor = flavor;
        }
        else {
            log.warn("Flavor type is not valid.");
        }

    }

    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.id = id;
    }

    //----------------------------------------------------------------------------------------------
    private void createServerRestCall()
    throws JSONException, IOException {
        RestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/servers";
        JSONObject data = new JSONObject();
        JSONObject server = new JSONObject();

        server.put("name", name);
        server.put("imageRef", image);
        server.put("flavorRef", Flavor.lookupFlavorID(flavor));
        data.put("server", server);
        log.debug("Sending body " + data.toString());
        PostMethod method = new PostMethod(uri);
        RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", null);
        method.setRequestEntity(entity);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());

        int status = client.invokeMethod(method);
        if (200 <= status && status <= 202) {
            log.info("Server creation request succeeded.");
            String body = client.getBody(method);
            JSONObject resultJSON = new JSONObject(body);
            id = resultJSON.getJSONObject("server").getString("id");
        }
        else {
            log.debug("Exception when creating server.");
            String body = client.getBody(method);
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    private void deleteServerRestCall()
    throws JSONException, IOException {
        RestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/servers/" + client.encodePath(id);
        log.debug("deletion uri: " + uri);
        DeleteMethod method = new DeleteMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());

        int status = client.invokeMethod(method);
        if (202 <= status && status <= 204) {
            log.info("Server deletion request succeeded.");
        }
        else {
            log.debug("Exception when deleting server.");
            String body = client.getBody(method);
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        log.info("creating server with name " + name + " from image " + image);
        createServerRestCall();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        log.info("deleting server with name " + name);
        deleteServerRestCall();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

}

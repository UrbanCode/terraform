package org.urbancode.terraform.tasks.rackspace;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
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
        server.put("image", image);
        server.put("flavor", Flavor.lookupFlavorID(flavor));
        data.put("server", server);

        PostMethod method = new PostMethod(uri);
        RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", null);
        method.setRequestEntity(entity);

        int status = client.invokeMethod(method);
        if (200 <= status && status <= 202) {
            log.info("Server request succeeded.");
        }
        else {
            log.debug("Exception when authenticating.");
            throw new IOException(String.format("%d %s",
                status,
                HttpStatus.getStatusText(status)));
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
        log.info("I pretended to destroy a server with name " + name + "!");
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

}

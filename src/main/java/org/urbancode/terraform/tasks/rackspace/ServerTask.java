package org.urbancode.terraform.tasks.rackspace;

import java.io.IOException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
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
    private boolean appendSuffix = false;//intentionally has no getter
    private String region;
    private String image;
    private String flavor;
    private String id;
    private String publicIp = "";
    private String privateIp = "";

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
    public String getPublicIp() {
        return publicIp;
    }

    //----------------------------------------------------------------------------------------------
    public String getPrivateIp() {
        return privateIp;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public void setAppendSuffix(boolean appendSuffix) {
        this.appendSuffix = appendSuffix;
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
        if (ServerFlavor.contains(flavor)) {
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
    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    //----------------------------------------------------------------------------------------------
    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    //----------------------------------------------------------------------------------------------
    private String resolveImageIDForImageName(String imageName)
    throws IOException, JSONException {
        String result = null;
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/images/detail";
        GetMethod method = new GetMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());
        method.setRequestHeader("Content-Type", "application/json");

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 204) {
            JSONArray imagesJSON = new JSONObject(body).getJSONArray("images");
            for (int i=0; i<imagesJSON.length(); i++) {
                JSONObject currentImage = imagesJSON.getJSONObject(i);
                String nameToTest = currentImage.getString("name");
                if (imageName.equals(nameToTest)) {
                    result = currentImage.getString("id");
                    break;
                }
            }
        }
        else {
            log.warn("Exception when fetching image IDs.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private JSONObject pollForServerStatus()
    throws IOException, JSONException {
        JSONObject result = null;
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/servers/" + client.encodePath(id);
        GetMethod method = new GetMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());
        method.setRequestHeader("Content-Type", "application/json");

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 204) {
            JSONObject serverJSON = new JSONObject(body).getJSONObject("server");
            result = serverJSON;
        }
        else {
            log.warn("Exception when polling server for status.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void updateIpAddresses(JSONObject addressesJSON)
    throws JSONException {
        JSONArray publicAddressArray = addressesJSON.getJSONArray("public");
        for (int i=0; i<publicAddressArray.length(); i++) {
            JSONObject publicAddressJSON = publicAddressArray.getJSONObject(i);
            if (publicAddressJSON.getInt("version") == 4) {
                publicIp = publicAddressJSON.getString("addr");
                break;
            }
        }
        JSONArray privateAddressArray = addressesJSON.getJSONArray("private");
        for (int i=0; i<privateAddressArray.length(); i++) {
            JSONObject privateAddressJSON = privateAddressArray.getJSONObject(i);
            if (privateAddressJSON.getInt("version") == 4) {
                privateIp = privateAddressJSON.getString("addr");
                break;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void createServerRestCall(String imageId)
    throws JSONException, IOException {
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/servers";
        JSONObject data = new JSONObject();
        JSONObject server = new JSONObject();

        server.put("name", name);
        server.put("imageRef", imageId);
        server.put("flavorRef", ServerFlavor.lookupFlavorID(flavor));
        data.put("server", server);
        log.debug("Sending body " + data.toString());
        PostMethod method = new PostMethod(uri);
        RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", null);
        method.setRequestEntity(entity);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 202) {
            log.info("Server creation request succeeded.");
            JSONObject resultJSON = new JSONObject(body);
            id = resultJSON.getJSONObject("server").getString("id");
        }
        else {
            log.warn("Exception when creating server.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    private void deleteServerRestCall()
    throws JSONException, IOException {
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".servers.api.rackspacecloud.com/v2/" +
        client.encodePath(client.getTenantID()) + "/servers/" + client.encodePath(id);
        log.debug("deletion uri: " + uri);
        DeleteMethod method = new DeleteMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (202 <= status && status <= 204) {
            log.info("Server deletion request succeeded.");
        }
        else {
            log.warn("Exception when deleting server.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        if (appendSuffix) {
            name = name + "-" + env.fetchSuffix();
        }
        String imageId;
        //Check if they supplied an image ID. If they didn't then try to match image name to an ID.
        if (image.matches("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) {
            imageId = image;
        }
        else {
            imageId = resolveImageIDForImageName(image);
        }
        log.info("Creating server with name " + name + " from image " + image);
        createServerRestCall(imageId);
        log.info("Server request succeeded. Polling for server to come online...");
        boolean active = false;
        long pollInterval = 5000L;
        while (!active) {
            Thread.sleep(pollInterval);
            JSONObject serverJSON = pollForServerStatus();
            if (serverJSON.getString("status").equalsIgnoreCase("ACTIVE")) {
                try {
                    updateIpAddresses(serverJSON.getJSONObject("addresses"));
                    env.fetchContext().setProperty(name + "-public-ip", publicIp);
                    env.fetchContext().setProperty(name + "-private-ip", privateIp);
                }
                catch(JSONException e) {
                    log.warn("Exception while finding IP addresses. Continuing...", e);
                }
                finally {
                    active = true;
                }
            }
        }
        log.info("Server " + name + " is online at IP " + publicIp);

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        log.info("deleting server with name " + name);
        deleteServerRestCall();
    }

}

package org.urbancode.terraform.tasks.rackspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class DatabaseTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(DatabaseTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskRackspace env;
    private String name;
    private boolean appendSuffix = false;
    private String region;
    private String flavor;
    private int volumeSize;
    private String id;
    private String hostname;
    private List<DatabaseUserTask> dbUsers = new ArrayList<DatabaseUserTask>();

    //----------------------------------------------------------------------------------------------
    public DatabaseTask(EnvironmentTaskRackspace env) {
        super();
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
    public String getFlavor() {
        return flavor;
    }

    //----------------------------------------------------------------------------------------------
    public int getVolumeSize() {
        return volumeSize;
    }

    //----------------------------------------------------------------------------------------------
    public String getId() {
        return id;
    }

    //----------------------------------------------------------------------------------------------
    public String getHostname() {
        return hostname;
    }

    //----------------------------------------------------------------------------------------------
    public List<DatabaseUserTask> getUsers() {
        return dbUsers;
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
    public void setFlavor(String flavor) {
        if (DatabaseFlavor.contains(flavor)) {
            this.flavor = flavor;
        }
        else {
            log.warn("Flavor type is not valid.");
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setVolumeSize(int volumeSize) {
        this.volumeSize = volumeSize;
    }

    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.id = id;
    }

    //----------------------------------------------------------------------------------------------
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    //----------------------------------------------------------------------------------------------
    public DatabaseUserTask createUser() {
        DatabaseUserTask user = new DatabaseUserTask();
        dbUsers.add(user);
        return user;
    }

    //----------------------------------------------------------------------------------------------
    private JSONObject pollForDatabaseStatus()
    throws IOException, JSONException {
        JSONObject result = null;
        RestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".databases.api.rackspacecloud.com/v1.0/" +
        client.encodePath(client.getTenantID()) + "/instances/" + client.encodePath(id);
        GetMethod method = new GetMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());
        method.setRequestHeader("Content-Type", "application/json");

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 204) {
            JSONObject serverJSON = new JSONObject(body).getJSONObject("instance");
            result = serverJSON;
        }
        else {
            log.warn("Exception when polling database for status.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private JSONArray generateUserJSON()
    throws JSONException {
        JSONArray result = new JSONArray();
        for (DatabaseUserTask user : dbUsers) {
            JSONObject userJSON = new JSONObject();
            JSONObject dbName = new JSONObject().put("name", name);
            userJSON.put("databases", new JSONArray().put(dbName));
            userJSON.put("name", user.getUsername());
            userJSON.put("password", user.fetchPassword());
            result.put(userJSON);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void createDbRestCall()
    throws JSONException, IOException {
        RestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".databases.api.rackspacecloud.com/v1.0/" +
        client.encodePath(client.getTenantID()) + "/instances";
        JSONObject data = new JSONObject();
        JSONObject instance = new JSONObject();

        instance.put("name", name);
        instance.put("volume", new JSONObject().put("size", volumeSize));

        String flavorRef = "https://" + region + ".databases.api.rackspacecloud.com/v1.0/";
        flavorRef = flavorRef + client.getTenantID() + "/flavors/" + DatabaseFlavor.lookupFlavorID(flavor);
        instance.put("flavorRef", flavorRef);

        JSONArray dbs = new JSONArray();
        JSONObject db = new JSONObject();
        db.put("name", name);
        db.put("character_set", "utf8");
        db.put("collate", "utf8_general_ci");
        dbs.put(db);
        instance.put("databases", dbs);

        instance.put("users", generateUserJSON());

        data.put("instance", instance);
        log.debug("Sending body " + data.toString());
        PostMethod method = new PostMethod(uri);
        RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", null);
        method.setRequestEntity(entity);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());
        method.setRequestHeader("Content-Type", "application/json");

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 202) {
            log.info("Database instance creation request succeeded.");
            JSONObject resultJSON = new JSONObject(body);
            id = resultJSON.getJSONObject("instance").getString("id");
            hostname = resultJSON.getJSONObject("instance").getString("hostname");
        }
        else {
            log.warn("Exception when creating database instance.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    private void deleteDbRestCall()
    throws JSONException, IOException {
        RestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".databases.api.rackspacecloud.com/v1.0/" +
        client.encodePath(client.getTenantID()) + "/instances/" + client.encodePath(id);

        DeleteMethod method = new DeleteMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());
        method.setRequestHeader("Content-Type", "application/json");

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 204) {
            log.info("Database instance deletion request succeeded.");
        }
        else {
            log.warn("Exception when deleting database instance.");
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
        try {
            createDbRestCall();
            log.info("Database request succeeded. Polling for database to come online...");
            boolean active = false;
            long pollInterval = 3000L;
            while (!active) {
                Thread.sleep(pollInterval);
                JSONObject dbJSON = pollForDatabaseStatus();
                if (dbJSON.getString("status").equalsIgnoreCase("ACTIVE")) {
                    active = true;
                }
            }
            log.info("The database is now active.");
        }
        catch(IOException e) {
            log.error("A REST call failed while creating database instance", e);
        }
        catch(JSONException e) {
            log.error("Malformed JSON while creating database instance", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        try {
            deleteDbRestCall();
        }
        catch(IOException e) {
            log.error("A REST call failed while creating database instance", e);
        }
        catch(JSONException e) {
            log.error("Malformed JSON while creating database instance", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

}

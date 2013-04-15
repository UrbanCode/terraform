package com.urbancode.terraform.tasks.rackspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.x2o.tasks.SubTask;

public class LoadBalancerTask extends SubTask {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(LoadBalancerTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskRackspace env;
    String id = null;
    String name;
    String region;
    String protocol;
    String port;
    String algorithm = "RANDOM";
    String ipType;//public or private
    boolean autoDelete = true;

    List<LoadBalancerNodeTask> nodes = new ArrayList<LoadBalancerNodeTask>();

    //----------------------------------------------------------------------------------------------
    public LoadBalancerTask(EnvironmentTaskRackspace env) {
        super();
        this.env = env;
    }

    //----------------------------------------------------------------------------------------------
    public String getId() {
        return id;
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
    public String getProtocol() {
        return protocol;
    }

    //----------------------------------------------------------------------------------------------
    public String getPort() {
        return port;
    }

    //----------------------------------------------------------------------------------------------
    public String getAlgorithm() {
        return algorithm;
    }

    //----------------------------------------------------------------------------------------------
    public String getIpType() {
        return ipType;
    }

    //----------------------------------------------------------------------------------------------
    public List<LoadBalancerNodeTask> getNodeTasks(){
        return nodes;
    }

    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.id = id;
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
            log.warn("You did not enter a valid region for this load balancer.");
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    //----------------------------------------------------------------------------------------------
    public void setPort(String port) {
        this.port = port;
    }

    //----------------------------------------------------------------------------------------------
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    //----------------------------------------------------------------------------------------------
    public void setIpType(String ipType) {
        this.ipType = ipType;
        if (!ipType.equalsIgnoreCase("public") && !ipType.equalsIgnoreCase("private")) {
            log.warn("Valid IP types for load balancers are \"public\" or \"private\"");
        }
    }

    //----------------------------------------------------------------------------------------------
    public LoadBalancerNodeTask createNode() {
        LoadBalancerNodeTask node = new LoadBalancerNodeTask(env, ipType);
        nodes.add(node);
        return node;
    }

    //----------------------------------------------------------------------------------------------
    private void createLBRestCall()
    throws JSONException, IOException {
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".loadbalancers.api.rackspacecloud.com/v1.0/" +
        client.encodePath(client.getTenantID()) + "/loadbalancers";
        JSONObject data = new JSONObject();
        JSONObject loadBalancer = new JSONObject();

        loadBalancer.put("name", name);
        loadBalancer.put("port", port);
        loadBalancer.put("protocol", protocol);
        loadBalancer.put("algorithm", algorithm);
        loadBalancer.put("nodes", generateNodeJSON());

        JSONArray virtualIps = new JSONArray();
        JSONObject virtualIp = new JSONObject();
        if (ipType.equalsIgnoreCase("public")) {
            virtualIp.put("type", "PUBLIC");
        }
        else if (ipType.equalsIgnoreCase("private") || ipType.equalsIgnoreCase("servicenet")) {
            virtualIp.put("type", "SERVICENET");
        }
        virtualIps.put(virtualIp);
        loadBalancer.put("virtualIps", virtualIps);

        data.put("loadBalancer", loadBalancer);
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
            log.info("Load balancer creation request succeeded.");
            JSONObject resultJSON = new JSONObject(body);
            id = resultJSON.getJSONObject("loadBalancer").getString("id");
        }
        else {
            log.warn("Exception when creating load balancer.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    private void deleteLBRestCall()
    throws JSONException, IOException {
        RackspaceRestClient client = env.fetchContext().client;
        String uri = "https://" + client.encodePath(region) + ".loadbalancers.api.rackspacecloud.com/v1.0/" +
        client.encodePath(client.getTenantID()) + "/loadbalancers/" + client.encodePath(id);
        DeleteMethod method = new DeleteMethod(uri);
        method.setRequestHeader("X-Auth-Token", client.getAuthToken());

        int status = client.invokeMethod(method);
        String body = client.getBody(method);
        method.releaseConnection();
        if (200 <= status && status <= 204) {
            log.info("Load balancer deletion request succeeded.");
        }
        else {
            log.warn("Exception when deleting load balancer.");
            throw new IOException(String.format("%d %s %s",
                status,
                HttpStatus.getStatusText(status),
                body));
        }
    }

    //----------------------------------------------------------------------------------------------
    private JSONArray generateNodeJSON()
    throws JSONException {
        JSONArray result = new JSONArray();
        for (LoadBalancerNodeTask node : nodes) {
            JSONObject nodeJSON = new JSONObject();
            nodeJSON.put("address", node.getAddress());
            nodeJSON.put("port", node.getPort());
            nodeJSON.put("condition", node.getCondition());
            result.put(nodeJSON);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        for (LoadBalancerNodeTask node : nodes) {
            node.create();
        }
        try {
            createLBRestCall();
        }
        catch(IOException e) {
            log.error("A REST call failed while creating load balancer", e);
        }
        catch(JSONException e) {
            log.error("Malformed JSON while creating load balancer", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        deleteLBRestCall();
    }

}

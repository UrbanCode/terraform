package org.urbancode.terraform.tasks.aws;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.EnvironmentTask;
import org.urbancode.terraform.tasks.common.MultiThreadTask;
import org.urbancode.terraform.tasks.util.InstancePriorityComparator;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.NetworkInterface;

public class EnvironmentTaskAWS extends EnvironmentTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(EnvironmentTaskAWS.class);
    static final private int MAX_THREADS = 30;
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;
    
    String xmlNs;
    private String name;
    private VpcTask vpc;
    private List<InstanceTask> instances = new ArrayList<InstanceTask>();
    private List<LoadBalancerTask> loadBalancers = new ArrayList<LoadBalancerTask>();
    
    // for timeouts
    private long pollInterval = 3000L;
    private long timeoutInterval = 15L * 60L * 1000L;
    private long start;
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskAWS(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
    }    
    
    //----------------------------------------------------------------------------------------------
    public InstanceTask findInstanceByName(String name) {
        InstanceTask result = null;
        if (instances != null && !instances.isEmpty()) {
            for (InstanceTask instance : instances) {
                if (instance.getName().equals(name)) {
                    result = instance;
                    break;
                }
            }
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public InstanceTask findInstanceById(String id) {
        InstanceTask result = null;
        if (instances != null && !instances.isEmpty()) {
            for (InstanceTask instance : instances) {
                if (instance.getId() != null && instance.getId().equals(id)) {
                    result = instance;
                    break;
                }
            }
        }
        
        return result;
    }
   
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public VpcTask getVpc() {
        return vpc;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<LoadBalancerTask> getLoadBalancers() {
        return Collections.unmodifiableList(loadBalancers);
    }
    
    //----------------------------------------------------------------------------------------------
    public List<InstanceTask> getInstances() {
        return Collections.unmodifiableList(instances);
    }
    
    //----------------------------------------------------------------------------------------------
    public VpcTask createVpc() {
        this.vpc = new VpcTask(context);
        return this.vpc;
    }
    
    //----------------------------------------------------------------------------------------------
    public InstanceTask createInstance() {
        InstanceTask inst = new InstanceTask(context);
        instances.add(inst);
        return inst;
    }
    
    //----------------------------------------------------------------------------------------------    
    public LoadBalancerTask createLoadBalancer() {
        LoadBalancerTask balancer = new LoadBalancerTask(context);
        loadBalancers.add(balancer);
        return balancer;
    }
    
    //----------------------------------------------------------------------------------------------
    public void terminateInstance(String instanceId) 
    throws Exception {
        findInstanceById(instanceId).destroy();
    }
    
    //----------------------------------------------------------------------------------------------
    private void handleInstances(List<InstanceTask> instances, boolean doCreate) 
    throws Exception {
        if (instances != null && !instances.isEmpty()) {
            // not sure if this is working as intended 
            int threadPoolSize = instances.size();
            if (threadPoolSize > MAX_THREADS) {
                threadPoolSize = MAX_THREADS;
            }
            
            // create instances - launch thread for each one
            List<MultiThreadTask> threadList = new ArrayList<MultiThreadTask>();
            ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
            start = System.currentTimeMillis();
            
            for (InstanceTask instance : instances) {
                if (doCreate) {
                    String subnetIn = getVpc().findSubnetForName(instance.getSubnetName()).getId();
                    instance.setSubnetId(subnetIn);
                }
                
                MultiThreadTask mThread = new MultiThreadTask(instance, doCreate, context);
                threadList.add(mThread);
                service.execute(mThread);
            }
            service.shutdown(); // accept no more threads
            
            while(!service.isTerminated()) {
                if (System.currentTimeMillis() - start > timeoutInterval) {
                    throw new RemoteException(
                            "Timeout waiting for creation Instance threads to finish");
                }
                // wait until all threads are done
                Thread.sleep(pollInterval);
            }
            
            // check for Exceptions caught in threads
            for (MultiThreadTask task : threadList) {
                if (task.getExceptions().size() != 0) {
                    for (Exception e : task.getExceptions()) {
                        log.error("Exception caught!", e);
                        throw e;
                    }
                }
            }
        }
        else {
            log.error("List of instances to launch was null!");
        }
    }
    
    //----------------------------------------------------------------------------------------------
    private void launchLoadBalancers(List<LoadBalancerTask> loadBalancers) 
    throws Exception {
        // create the loadBalancer(s)
        if (loadBalancers != null && !loadBalancers.isEmpty()) {
            for (LoadBalancerTask loadBalancer : loadBalancers) {
                loadBalancer.create();
            }
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws EnvironmentCreationException {
        log.debug("EnvironmentAWS: create()");
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        log.info("Creating Environment...");
        
        setStartTime(System.currentTimeMillis());
        
        try {
            getVpc().create();
            
            // launch load balancers before we launch instances
            // we do this so we can just hold a ref to lb on the 
            // instance and we need to register the it on the lb.
            launchLoadBalancers(loadBalancers);
            
            if (instances != null) {
                List<InstanceTask> newInstances = new ArrayList<InstanceTask>();
                for (InstanceTask instance : instances) {
                    instance.setName(instance.getName() + "0");
                    InstanceTask newInstance = null;
                    // make a list of all the new instances
                    for (int i=1; i<instance.getCount(); i++) {
                        newInstance = instance.clone();
                        newInstance.setName(instance.getName() + i); 
                        // TODO - update EBS names?
                        newInstances.add(newInstance);
                    }
                    // add 0
                    instance.setName(instance.getName() + "0");
                    instance.setCount(1);
                }
                // add new instances to old instances
                instances.addAll(newInstances);
            }
            
            // setup launch groups
            Comparator<InstanceTask> comparer = new InstancePriorityComparator();
            PriorityQueue<InstanceTask> queue = new PriorityQueue<InstanceTask>(3, comparer);
            
            for (InstanceTask instance : getInstances()) {
                queue.add(instance);
            }
            
            InstanceTask currentInst;
            // get instance at first of queue, continue if not null...
            while ((currentInst = queue.poll())!= null) {
                // create new LaunchGroup
                List<InstanceTask> launchGroup = new ArrayList<InstanceTask>();
                // add instance to launchGroup
                launchGroup.add(currentInst);
                // get current priority of instance
                int currentPri = currentInst.getPriority();
                
                // get priority of next instance, see if it matches current priority
                if (queue.peek() != null && currentPri == queue.peek().getPriority()) {
                    // if so, takeit out of the queue and add to launchGroup
                    launchGroup.add(queue.poll());
                }
                // otherwise we have a new priorty
                else {
                    // if the group has members
                    if (launchGroup != null && !launchGroup.isEmpty()) {
                        // launch the group
                        String msg = "Launching Instances: ";
                        msg += "\n\tGroup: " + launchGroup.get(0).getPriority();
                        for (InstanceTask toLaunch : launchGroup) {
                            msg += "\n\t\t" + toLaunch.getName();
                        }
                        log.info(msg);
                        
                        handleInstances(launchGroup, true);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new EnvironmentCreationException("Failed to create environment!", e);
        }
        finally {
            ec2Client = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() 
    throws EnvironmentDestructionException {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        int threadPoolSize = instances.size();
        if (threadPoolSize > MAX_THREADS) {
            threadPoolSize = MAX_THREADS;
        }
        
        try {
            
            // detach any ENIs that we can
            List<NetworkInterface> interfaces = helper.describeNetworkInterfaces(null, getVpc().getId(), ec2Client);
            List<String> detachIds = new ArrayList<String>();
            for (NetworkInterface iface : interfaces) {
                if (iface.getAttachment() != null && iface.getAttachment().getDeviceIndex() != 0) {
                    detachIds.add(iface.getAttachment().getAttachmentId());
                }
            }
            helper.detachNetworkInterfaces(detachIds, ec2Client);
            
            // destroy instances
            handleInstances(instances, false);
            
            // destroy load balancers
            if (getLoadBalancers() != null && !getLoadBalancers().isEmpty()) {
                for (LoadBalancerTask loadBalancer : getLoadBalancers()) {
                    loadBalancer.destroy();
                }
            }
            
            // kill vpc
            getVpc().destroy();
        }
        catch (Exception e) {
            throw new EnvironmentDestructionException("Failed to destroy environment!", e);
        }
        finally {
            ec2Client = null;
        }
    }
}

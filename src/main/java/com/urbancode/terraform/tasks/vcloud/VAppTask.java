package com.urbancode.terraform.tasks.vcloud;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.savvis.sdk.oauth.connections.HttpApiResponse;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import com.urbancode.x2o.tasks.SubTask;
import com.urbancode.x2o.xml.XmlParsingException;

public class VAppTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(VAppTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private EnvironmentTaskVCloud env;
    private List<VMTask> vmTasks = new ArrayList<VMTask>();
    
    private String name;
    private String templateName;
    private String catalogName;
    private String templateId;
    private String description;
    private String id;
    
    //----------------------------------------------------------------------------------------------
    public VAppTask(EnvironmentTaskVCloud env) {
        super();
        this.env = env;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<VMTask> getVmTasks() {
        return vmTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getTemplateName() {
        return templateName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getCatalogName() {
        return catalogName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getDescription() {
        return description;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return id;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getTemplateId() { 
        return templateId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setDescription(String description) {
        this.description = description;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.id = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public VMTask createVm() {
        VMTask vmTask = new VMTask();
        vmTasks.add(vmTask);
        return vmTask;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws Exception {
        if (templateId == null) {
            findTemplateId();
        }
        String suffixString = "-" + env.fetchSuffix();
        name = name.contains(suffixString) ? name : name + suffixString;
        for (VMTask vmTask : vmTasks) {
            vmTask.create();
        }
        String requestBody = generateCreateRequest();
        String contentType = "application/vnd.vmware.vcloud.instantiateVAppTemplateParams+xml";
        String urlSuffix = "/vdc/" + env.getVdcId() + "/action/instantiateVAppTemplate";
        HttpApiResponse response = null;
        
        try {
            response = SavvisClient.getInstance().makeApiCallWithSuffix(urlSuffix, 
                    SavvisClient.POST_METHOD, requestBody, contentType);
        }
        catch (Exception e) {
            env.fetchContext().setWriteContext(false);
            throw e;
        }
        
        log.debug("response: " + response.getResponseString());
        id = findHref(response.getResponseString());
        
        try {
            log.debug("Finding VMs...");
            boolean foundVMs = false;
            boolean taskDone = false;
            long timeout = System.currentTimeMillis() + 720000L;
            while (!foundVMs) {
                log.trace("Waiting for VMs to be created.");
                Thread.sleep(5000);
                HttpApiResponse vAppResponse = fetchVAppResponse();
                foundVMs = unmarshalVMs(vAppResponse.getResponseString());
                if (!foundVMs && System.currentTimeMillis() > timeout) {
                    throw new EnvironmentCreationException("Timeout waiting for vApp to create VMs");
                }
            }
            while (!taskDone) {
                log.trace("Waiting for VMs to be started.");
                Thread.sleep(5000);
                HttpApiResponse vAppResponse = fetchVAppResponse();
                taskDone = isCreateTaskDone(vAppResponse.getResponseString());
                if (!taskDone && System.currentTimeMillis() > timeout) {
                    throw new EnvironmentCreationException("Timeout waiting for vApp to start");
                }
            }

        }
        catch (EnvironmentCreationException e) {
            throw e;
        }
        catch (Exception e) {
            log.debug("error while finding VMs from vApp", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        for (VMTask vmTask : vmTasks) {
            vmTask.destroy();
        }
        HttpApiResponse response = fetchVAppResponse();
        log.trace("response: " + response.getResponseString());
        
        response = SavvisClient.getInstance().makeApiCallWithSuffix("/vApp/" + id + "/action/undeploy", 
                SavvisClient.POST_METHOD, undeployBody(), "application/vnd.vmware.vcloud.undeployVAppParams+xml");
        log.trace("response: " + response.getResponseString());
        
        boolean found = false;
        long timeout = System.currentTimeMillis() + 180000L;
        while (!found) {
            log.trace("Waiting for undeployment.");
            Thread.sleep(5000);
            response = SavvisClient.getInstance().makeApiCallWithSuffix("/vApp/" + id, 
                    SavvisClient.GET_METHOD, "", "");
            try {
                found = hasRemoveLink(response.getResponseString());
            }
            catch (Exception e) {
                log.warn("Exception while checking to see if vApp was undeployed", e);
            }
            if (!found && System.currentTimeMillis() > timeout) {
                throw new EnvironmentDestructionException("Timeout waiting for vApp to undeploy");
            }
        }
        
        response = SavvisClient.getInstance().makeApiCallWithSuffix("/vApp/" + id, 
                SavvisClient.DELETE_METHOD, "", "");
        log.trace("response: " + response.getResponseString());
    }
    
    //----------------------------------------------------------------------------------------------
    private HttpApiResponse fetchVAppResponse() throws Exception {
        return SavvisClient.getInstance().makeApiCallWithSuffix("/vApp/" + id, 
                SavvisClient.GET_METHOD, "", "");
    }
    
    //----------------------------------------------------------------------------------------------
    public String generateCreateRequest() 
    throws ParserConfigurationException, TransformerException {
        Document resultDoc = env.blankDocument();
        resultDoc.setXmlVersion("1.0");
        Element root = resultDoc.createElement("InstantiateVAppTemplateParams");
        resultDoc.appendChild(root);
        root.setAttribute("xmlns", "http://www.vmware.com/vcloud/v1.5");
        root.setAttribute("name", name);
        root.setAttribute("deploy", String.valueOf(true));
        root.setAttribute("powerOn", String.valueOf(true));
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:ovf", "http://schemas.dmtf.org/ovf/envelope/1");
        
        if (!StringUtils.isEmpty(description)) {
            Element descriptionElement = resultDoc.createElement("Description");
            descriptionElement.setTextContent(description);
            root.appendChild(descriptionElement);
        }
        
        Element source = resultDoc.createElement("Source");
        String baseUrl = SavvisClient.getInstance().getCredentials().getApiBaseLocation();
        String cleanTemplateId = cleanTemplateId();
        source.setAttribute("href", baseUrl + "/vAppTemplate/" + cleanTemplateId);
        root.appendChild(source);
        
        Element eulas = resultDoc.createElement("AllEULAsAccepted");
        eulas.setTextContent(String.valueOf(true));
        root.appendChild(eulas);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(resultDoc), new StreamResult(writer));
        String result = writer.getBuffer().toString().replaceAll("\n|\r", "");
        log.trace(result);
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private String findHref(String vAppBody) throws XmlParsingException {
        String result = null;
        try {
            Document doc = env.buildDocument(vAppBody);
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression networkQuery = xpath.compile("/VApp/@href");
            Object preResult = networkQuery.evaluate(doc, XPathConstants.STRING);
            String href = (String) preResult;
            result = href.substring(href.indexOf("vapp-"));
        } 
        catch (Exception e) {
            log.error("exception while finding vApp link", e);
            throw new XmlParsingException(e);
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private boolean hasRemoveLink(String vAppBody) 
    throws XmlParsingException {
        boolean result = false;
        
        try {
            Document doc = env.buildDocument(vAppBody);
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression networkQuery = xpath.compile("/VApp/Link[@rel='remove']/@href");
            String preResult = (String) networkQuery.evaluate(doc, XPathConstants.STRING);
            if (preResult.contains(id)) {
                result = true;
            }
        } 
        catch (Exception e) {
            log.error("exception while finding vApp link", e);
            throw new XmlParsingException(e);
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    protected Node getNetworkConfigForTemplate(Document templateDoc) 
    throws XPathExpressionException {
        Node result;
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression networkQuery = xpath.compile("//NetworkConfigSection");
        Object preResult = networkQuery.evaluate(templateDoc, XPathConstants.NODE);
        if (preResult == null) {
            throw new XPathExpressionException("The network config node could not be found.");
        }
        result = (Node) preResult;
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    protected Document fetchvAppTemplate(String templateToFetch) 
    throws Exception {
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                "/vAppTemplate/" + templateToFetch, 
                SavvisClient.GET_METHOD, "", "");
        String responseBody = response.getResponseString();
        
        return env.buildDocument(responseBody);
    }
    
    //----------------------------------------------------------------------------------------------
    private String cleanTemplateId() {
        String cleanTemplateId = templateId;
        if (!cleanTemplateId.contains("vappTemplate")) {
            cleanTemplateId = "vappTemplate-" + cleanTemplateId;
        }
        return cleanTemplateId;
    }
    
    //----------------------------------------------------------------------------------------------
    private String undeployBody() 
    throws ParserConfigurationException, TransformerException {
        String result = "";
        Document doc = env.blankDocument();
        Element undeployParams = doc.createElement("UndeployVAppParams");
        undeployParams.setAttribute("xmlns", "http://www.vmware.com/vcloud/v1.5");
        Element undeployAction = doc.createElement("UndeployPowerAction");
        undeployAction.setTextContent("powerOff");
        undeployParams.appendChild(undeployAction);
        doc.appendChild(undeployParams);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        result = writer.getBuffer().toString().replaceAll("\n|\r", "");
        log.trace("sending undeploy body: " + result);
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    /**
     * @param vAppBody the XML response from the GET request
     * @return true if VMs were found, false otherwise
     */
    private boolean unmarshalVMs(String vAppBody) 
    throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        log.trace(vAppBody);
        boolean result = false;
        Document doc = env.buildDocument(vAppBody);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression networkQuery = xpath.compile("/VApp/Children/Vm");
        Object preResult = networkQuery.evaluate(doc, XPathConstants.NODESET);
        
        NodeList childrenNodes = (NodeList) preResult;
        result = childrenNodes.getLength() > 0;
        
        Set<String> currentVMNames = new HashSet<String>();
        for (VMTask vmTask : vmTasks) {
            currentVMNames.add(vmTask.getName());
        }
        for (int i=0; i<childrenNodes.getLength(); i++) {
            Element vmElement = (Element) childrenNodes.item(i);
            String vmName = vmElement.getAttribute("name");
            String vmHref = vmElement.getAttribute("href");
            if (!currentVMNames.contains(vmName)) {
                VMTask vm = createVm();
                vm.setName(vmName);
                vm.setHref(vmHref);
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private boolean isCreateTaskDone(String vAppBody) 
    throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        log.trace(vAppBody);
        boolean result = true;
        Document doc = env.buildDocument(vAppBody);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression networkQuery = xpath.compile("/VApp/Tasks/Task");
        Object preResult = networkQuery.evaluate(doc, XPathConstants.NODESET);
        NodeList childrenNodes = (NodeList) preResult;
        for (int i=0; i<childrenNodes.getLength(); i++) {
            Element vmElement = (Element) childrenNodes.item(i);
            String taskOperation = vmElement.getAttribute("operationName");
            if ("vdcInstantiateVapp".equals(taskOperation)) {
                log.debug("The vApp has started.");
                result = false;
                break;
            }
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private String findCatalogId() 
    throws Exception {
        String result = null;
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                "/org/" + env.fetchOrgId(), "GET", "", "");
        try {
            Document doc = env.buildDocument(response.getResponseString());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression query = xpath.compile("/Org/Link");
            Object preResult = query.evaluate(doc, XPathConstants.NODESET);
            NodeList childrenNodes = (NodeList) preResult;
            for (int i=0; i<childrenNodes.getLength(); i++) {
                Element linkElement = (Element) childrenNodes.item(i);
                String foundName = linkElement.getAttribute("name");
                if (catalogName.equals(foundName)) {
                    String href = linkElement.getAttribute("href");
                    result = href.substring(href.indexOf("/catalog"));
                    break;
                }
            }
        } 
        catch (Exception e) {
            log.error("exception while finding catalog ID", e);
            throw new XmlParsingException(e);
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private String findCatalogItemId() 
    throws Exception {
        String result = null;
        String catalogId = findCatalogId();
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                catalogId, "GET", "", "");
        try {
            Document doc = env.buildDocument(response.getResponseString());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression query = xpath.compile("/Catalog/CatalogItems/CatalogItem");
            Object preResult = query.evaluate(doc, XPathConstants.NODESET);
            NodeList childrenNodes = (NodeList) preResult;
            for (int i=0; i<childrenNodes.getLength(); i++) {
                Element linkElement = (Element) childrenNodes.item(i);
                String foundName = linkElement.getAttribute("name");
                if (templateName.equals(foundName)) {
                    String href = linkElement.getAttribute("href");
                    result = href.substring(href.indexOf("/catalogItem"));
                    break;
                }
            }
        } 
        catch (Exception e) {
            log.error("exception while finding catalog item ID", e);
            throw new XmlParsingException(e);
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private void findTemplateId() 
    throws Exception {
        String catalogItemId = findCatalogItemId();
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                catalogItemId, "GET", "", "");
        try {
            Document doc = env.buildDocument(response.getResponseString());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression query = xpath.compile("/CatalogItem/Entity");
            Object preResult = query.evaluate(doc, XPathConstants.NODE);
            Element entityElement = (Element) preResult;
            String href = entityElement.getAttribute("href");
            templateId = href.substring(href.indexOf("/vAppTemplate/") + "/vAppTemplate/".length());
        } 
        catch (Exception e) {
            log.error("exception while finding catalog item ID", e);
            throw new XmlParsingException(e);
        }
    }
    
}

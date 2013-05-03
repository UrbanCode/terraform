package com.urbancode.terraform.tasks.vcloud;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.savvis.sdk.oauth.connections.HttpApiResponse;
import com.urbancode.terraform.tasks.common.EnvironmentTask;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentCreationException;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentDestructionException;
import com.urbancode.terraform.tasks.common.exceptions.EnvironmentRestorationException;
import com.urbancode.x2o.xml.XmlParsingException;

public class EnvironmentTaskVCloud extends EnvironmentTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(EnvironmentTaskVCloud.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<VAppTask> vAppTasks = new ArrayList<VAppTask>();
    
    private String orgId;
    private String vdcId;
    private String vdcName;
    
    //----------------------------------------------------------------------------------------------
    public EnvironmentTaskVCloud(TerraformContext context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public List<VAppTask> getVAppTasks() {
        return vAppTasks;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getVdcId() {
        return vdcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getVdcName() {
        return vdcName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVdcId(String vdcId) {
        this.vdcId = vdcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVdcName(String vdcName) {
        this.vdcName = vdcName;
    }
    
    //----------------------------------------------------------------------------------------------
    public VAppTask createVApp() {
        VAppTask vApp = new VAppTask(this);
        vAppTasks.add(vApp);
        return vApp;
    }
    
    //----------------------------------------------------------------------------------------------
    protected String fetchOrgId() {
        return orgId;
    }
    
    //----------------------------------------------------------------------------------------------
    public TerraformContext fetchContext() {
        return (TerraformContext) this.context;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() throws EnvironmentCreationException {
        if (vdcId == null && vdcName != null) {
            try {
                findVdcIdForName();
            } catch (Exception e) {
                throw new EnvironmentCreationException(e);
            }
        }
        for (VAppTask vAppTask : vAppTasks) {
            try {
                vAppTask.create();
            } catch (Exception e) {
                log.error("Exception while creating vCloud environment", e);
                throw new EnvironmentCreationException(e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws EnvironmentRestorationException {
        // TODO Auto-generated method stub
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws EnvironmentDestructionException {
        for (VAppTask vAppTask : vAppTasks) {
            try {
                vAppTask.destroy();
            } catch (Exception e) {
                log.error("Exception while destroying vCloud environment", e);
                throw new EnvironmentDestructionException(e);
            }
        }
    }
    
    //----------------------------------------------------------------------------------------------
    private void findOrgIdForName() 
    throws Exception {
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                "/org/", "GET", "", "");
        try {
            Document doc = buildDocument(response.getResponseString());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression query = xpath.compile("/OrgList/Org");
            Object preResult = query.evaluate(doc, XPathConstants.NODESET);
            NodeList childrenNodes = (NodeList) preResult;
            log.trace ("found " + childrenNodes.getLength() + " nodes");
            String orgName = SavvisClient.getInstance().getOrganizationName();
            for (int i=0; i<childrenNodes.getLength(); i++) {
                Element orgElement = (Element) childrenNodes.item(i);
                String foundName = orgElement.getAttribute("name");
                if (orgName.equalsIgnoreCase(foundName)) {
                    String href = orgElement.getAttribute("href");
                    orgId = href.substring(href.indexOf("/org/")+5);
                    break;
                }
            }
        } 
        catch (Exception e) {
            log.error("exception while finding organization ID", e);
            throw new XmlParsingException(e);
        }
    }
    
    //----------------------------------------------------------------------------------------------
    private void findVdcIdForName() 
    throws Exception {
        if (orgId == null) {
            findOrgIdForName();
        }
        HttpApiResponse response = SavvisClient.getInstance().makeApiCallWithSuffix(
                "/org/" + orgId, "GET", "", "");
        try {
            Document doc = buildDocument(response.getResponseString());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression query = xpath.compile("/Org/Link");
            Object preResult = query.evaluate(doc, XPathConstants.NODESET);
            NodeList childrenNodes = (NodeList) preResult;
            log.trace ("found " + childrenNodes.getLength() + " nodes");
            for (int i=0; i<childrenNodes.getLength(); i++) {
                Element linkElement = (Element) childrenNodes.item(i);
                String foundName = linkElement.getAttribute("name");
                if (vdcName.equals(foundName)) {
                    String href = linkElement.getAttribute("href");
                    vdcId = href.substring(href.indexOf("/vdc/")+5);
                    break;
                }
            }
        } 
        catch (Exception e) {
            log.error("exception while finding VDC ID", e);
            throw new XmlParsingException(e);
        }
    }
    
    //----------------------------------------------------------------------------------------------
    protected Document buildDocument(String xmlBody) 
    throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlBody)));
    }
    
    //----------------------------------------------------------------------------------------------
    protected Document blankDocument() 
    throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().newDocument();
    }
}

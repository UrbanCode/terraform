package com.urbancode.uprovision.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.urbancode.uprovision.credentials.Credentials;
import com.urbancode.uprovision.credentials.CredentialsFactory;
import com.urbancode.uprovision.credentials.CredentialsParserRegistry;
import com.urbancode.uprovision.credentials.aws.CredentialsAWS;
import com.urbancode.uprovision.credentials.aws.CredentialsParserAWS;
import com.urbancode.uprovision.credentials.vmware.CredentialsParserVmware;
import com.urbancode.uprovision.credentials.vmware.CredentialsVmware;
import com.urbancode.uprovision.tasks.common.Context;
import com.urbancode.uprovision.tasks.util.Property;
import com.urbancode.uprovision.tasks.util.PropertyResolver;
import com.urbancode.uprovision.xml.XmlModelParser;
import com.urbancode.uprovision.xml.XmlParsingException;
import com.urbancode.uprovision.xml.XmlWrite;

public class Main {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(Main.class);

    //----------------------------------------------------------------------------------------------
    static public void main(String[] args)
    throws Exception {
        File inputXmlFile = null;
        File creds = null;
        List<String> unparsedArgs = new ArrayList<String>();
        boolean doCreate;

        if (args != null && args.length >= 3) {
            if ("destroy".equalsIgnoreCase(args[0])) {
                doCreate = false;
            }
            else if ("create".equalsIgnoreCase(args[0])) {
                doCreate = true;
            }
            else {
                String msg = "Invalid first argument: "+args[0];
                log.fatal(msg);
                throw new Exception(msg);
            }
            
            inputXmlFile = createFile(args[1]);
            creds = createFile(args[2]);

            Collections.addAll(unparsedArgs, args);
            // make args just the unparsed properties
            unparsedArgs.remove(0); // remove inputxmlpath
            unparsedArgs.remove(0); // remove create/destroy
            unparsedArgs.remove(0); // remove creds
        }
        else {
            log.fatal("Invalid number of arguments!\n" +
                    "Found " + args.length + "\n" +
                    "Expected at least 3");
            throw new Exception("args");
        }

        Main myMain = new Main(doCreate, inputXmlFile, creds, unparsedArgs);
        myMain.execute();
    }
    
    static private File createFile(String filePath) 
    throws Exception {
        File result = null;
        
        if (!"".equals(filePath)) {
        result = new File(filePath);
            if (result.exists()) {
                // the File exists
                // is it a file?
                if (result.isFile()) {
                    // it's a file
                    // is it readable?
                    if (!result.canRead()) {
                        String msg = "Input file does not exist: "+filePath;
                        log.fatal(msg);
                        throw new Exception(msg);
                    }
                }
                else {
                    String msg = "Input file is not a file: "+filePath;
                    log.fatal(msg);
                    throw new Exception(msg);
                }
            }
            else {
                // it doesn't exist
                String msg = "Input file does not exist: "+filePath;
                log.fatal(msg);
                throw new Exception(msg);
            }
        }
        
        return result;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private boolean isCreate;
    private File inputXmlFile;
    private File outputXmlFile;
    private File credsFile;
    private List<Property> props;

    //----------------------------------------------------------------------------------------------
    private Main(boolean create, File inputXmlFile, File credsFile, List<String> unparsed) {
        startCredsParser();
        this.isCreate = create;
        this.credsFile = credsFile;
        this.inputXmlFile = inputXmlFile;
        props = new ArrayList<Property>();
        if (unparsed != null) {
            for (String prop : unparsed) {
                try {
                    log.debug("Parsing property: " + prop);
                    props.add(parseProperty(prop));
                }
                catch (Exception e) {
                    log.error("Unable to parse property: " + prop);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void execute()
    throws Exception {
        // parse xml and set context
        Context context = parseContext(inputXmlFile);
        
        Credentials credentials = CredentialsFactory.getInstance().restoreFromFile(credsFile);
        context.setCredentials(credentials);
        
        log.debug("Create = " + isCreate);
        if (isCreate) {
            log.debug("Calling create() on context");
            context.create();

            // create new file if creating a new environment
            outputXmlFile = new File("env-" + context.getEnvironment().getName() + "-" +
                                 UUID.randomUUID().toString().substring(0,4) + ".xml");
        }
        else {
            log.debug("Calling destroy() on context");
            context.destroy();
            // update the input file if destroying
            outputXmlFile = inputXmlFile;
        }
        
        writeEnvToXml(outputXmlFile, context);
    }
    
    //----------------------------------------------------------------------------------------------
    private PropertyResolver createResolver() {
        return new PropertyResolver(props);
    }
    
    //----------------------------------------------------------------------------------------------
    private Context parseContext(File xmlFileToRead) 
    throws ParserConfigurationException, XmlParsingException, SAXException, IOException {
        Context result = null;
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFileToRead);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
 
        XmlModelParser parser = new XmlModelParser();
        result = parser.parse(rootElement);
        if (result != null) {
            result.setResolver(createResolver());
        }
        
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private Property parseProperty(String arg)
    throws Exception {
        log.debug("Parsing: " + arg);
        String[] args;
        Property result = null;
        if (arg != null && arg.contains("=") && (args = arg.split("=")).length == 2) {
            result = new Property(args[0], args[1]);
            log.info("Property: " + result.getName() + "=" + result.getValue());
        }
        else {
            log.error("bad property! Check your format. \nFound: " + arg);
            throw new Exception("bad parameter format");
        }
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    public void writeEnvToXml(File file, Context context)
    throws Exception {
        XmlWrite write = new XmlWrite();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        write.makeXml(context, doc, null);
        write.writeDocToFile(file, doc);
    }
    
    //----------------------------------------------------------------------------------------------
    private void startCredsParser() {
        CredentialsParserRegistry.getInstance().register(CredentialsAWS.class.getName(), CredentialsParserAWS.class);
        CredentialsParserRegistry.getInstance().register(CredentialsVmware.class.getName(), CredentialsParserVmware.class);
    }
}

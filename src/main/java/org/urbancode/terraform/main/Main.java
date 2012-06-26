/*******************************************************************************
 * Copyright 2012 Urbancode, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.urbancode.terraform.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsException;
import org.urbancode.terraform.credentials.CredentialsParser;
import org.urbancode.terraform.credentials.CredentialsParserRegistry;
import org.urbancode.terraform.credentials.aws.CredentialsAWS;
import org.urbancode.terraform.credentials.aws.CredentialsParserAWS;
import org.urbancode.terraform.credentials.vmware.CredentialsParserVmware;
import org.urbancode.terraform.credentials.vmware.CredentialsVmware;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.util.Property;
import org.urbancode.terraform.tasks.util.PropertyResolver;
import org.urbancode.terraform.xml.XmlModelParser;
import org.urbancode.terraform.xml.XmlParsingException;
import org.urbancode.terraform.xml.XmlWrite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class Main {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(Main.class);

    //----------------------------------------------------------------------------------------------
    static public void main(String[] args)
    throws IOException, XmlParsingException, CredentialsException {
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
                throw new IOException(msg);
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
            throw new IOException("improper args");
        }

        Main myMain = new Main(doCreate, inputXmlFile, creds, unparsedArgs);
        myMain.execute();
    }

    static private File createFile(String filePath) throws FileNotFoundException {
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
                        throw new FileNotFoundException(msg);
                    }
                }
                else {
                    String msg = "Input file is not a file: "+filePath;
                    log.fatal(msg);
                    throw new FileNotFoundException(msg);
                }
            }
            else {
                // it doesn't exist
                String msg = "Input file does not exist: "+filePath;
                log.fatal(msg);
                throw new FileNotFoundException(msg);
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
    throws XmlParsingException, IOException, CredentialsException {
        try {
            // parse xml and set context
            Context context = parseContext(inputXmlFile);
            
            Credentials credentials = parseCredentials(credsFile);
            
            context.setCredentials(credentials);

            log.debug("Create = " + isCreate);
            if (isCreate) {
                log.debug("Calling create() on context");
                context.create();

                // create new file if creating a new environment
                outputXmlFile = new File("env-" + context.getEnvironment().getName() + "-" +
                                     UUID.randomUUID().toString().substring(0,4) + ".xml");
                writeEnvToXml(outputXmlFile, context);
            }
            else {
                log.debug("Calling destroy() on context");
                context.destroy();
                inputXmlFile.delete();
            }
        }
        catch(ParserConfigurationException e1) {
            throw new XmlParsingException("ParserConfigurationException: " + e1.getMessage(), e1);
        }
        catch(SAXException e2) {
            throw new XmlParsingException("SAXException: " + e2.getMessage(), e2);
        }
    }

    //----------------------------------------------------------------------------------------------
    private PropertyResolver createResolver() {
        return new PropertyResolver(props);
    }
    
    //----------------------------------------------------------------------------------------------
    private Credentials parseCredentials(File credsFile) {
        Credentials result = null;

        Properties props = loadPropertiesFromFile(credsFile);

        result = parseCredsFromProps(props);

        if (result != null) {
            log.info("Restored Credentials: " + credsFile + " : " + result.getName());
        }
        else {
            log.info("Did not restore Credentials for " + credsFile);
        }

        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private Properties loadPropertiesFromFile(File propFile) {
        Properties result = new Properties();
        
        String path = propFile.getAbsolutePath();
        
        InputStream in = null;
        try {
            in = new FileInputStream(propFile);
            result.load(in);
        }
        catch (FileNotFoundException e) {
            log.error("Unable to load properties from " + path, e);
        // TODO - throw
        }
        catch (IOException e) {
            log.error("IOException when loading properties from " + path, e);
        // swallow
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
          catch (IOException e) {
                // swallow
            }
        }
    
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    private Credentials parseCredsFromProps(Properties props) {
        Credentials result = null;

        String type = props.getProperty("type");

        if (type == null || "".equals(type)) {
            throw new NullPointerException("No credentials type specified in props: " + props);
        }

        CredentialsParser parser = CredentialsParserRegistry.getInstance().getParser(type);

        result = parser.parse(props);

        return result;
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
    throws IOException {
        log.debug("Parsing: " + arg);
        String[] args;
        Property result = null;
        if (arg != null && arg.contains("=") && (args = arg.split("=")).length == 2) {
            result = new Property(args[0], args[1]);
            log.info("Property: " + result.getName() + "=" + result.getValue());
        }
        else {
            log.error("bad property! Check your format. \nFound: " + arg);
            throw new IOException("bad parameter format");
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void writeEnvToXml(File file, Context context)
    throws XmlParsingException {
        try {
            XmlWrite write = new XmlWrite();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            write.makeXml(context, doc, null);
            write.writeDocToFile(file, doc);
        }
        catch(Exception e) {
            throw new XmlParsingException("Exception while writing out XML: " + e.getMessage(), e);
        }

    }

    //----------------------------------------------------------------------------------------------
    private void startCredsParser() {
        CredentialsParserRegistry.getInstance().register(CredentialsAWS.class.getName(), CredentialsParserAWS.class);
        CredentialsParserRegistry.getInstance().register(CredentialsVmware.class.getName(), CredentialsParserVmware.class);
    }
}


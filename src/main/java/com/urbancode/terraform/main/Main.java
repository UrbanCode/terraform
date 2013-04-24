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
package com.urbancode.terraform.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.urbancode.terraform.commands.vmware.ResumeCommand;
import com.urbancode.terraform.commands.vmware.SuspendCommand;
import com.urbancode.terraform.commands.vmware.TakeSnapshotCommand;
import com.urbancode.terraform.credentials.aws.CredentialsAWS;
import com.urbancode.terraform.credentials.aws.CredentialsParserAWS;
import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsException;
import com.urbancode.terraform.credentials.common.CredentialsParser;
import com.urbancode.terraform.credentials.common.CredentialsParserRegistry;
import com.urbancode.terraform.credentials.microsoft.CredentialsMicrosoft;
import com.urbancode.terraform.credentials.microsoft.CredentialsParserMicrosoft;
import com.urbancode.terraform.credentials.rackspace.CredentialsParserRackspace;
import com.urbancode.terraform.credentials.rackspace.CredentialsRackspace;
import com.urbancode.terraform.credentials.vcloud.CredentialsParserVCloud;
import com.urbancode.terraform.credentials.vcloud.CredentialsVCloud;
import com.urbancode.terraform.credentials.vmware.CredentialsParserVmware;
import com.urbancode.terraform.credentials.vmware.CredentialsVmware;
import com.urbancode.terraform.tasks.aws.ContextAWS;
import com.urbancode.terraform.tasks.common.TerraformContext;
import com.urbancode.terraform.tasks.vmware.ContextVmware;
import com.urbancode.x2o.tasks.CreationException;
import com.urbancode.x2o.tasks.DestructionException;
import com.urbancode.x2o.tasks.RestorationException;
import com.urbancode.x2o.util.Property;
import com.urbancode.x2o.util.PropertyResolver;
import com.urbancode.x2o.xml.XmlModelParser;
import com.urbancode.x2o.xml.XmlParsingException;
import com.urbancode.x2o.xml.XmlWrite;


public class Main {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(Main.class);

    //----------------------------------------------------------------------------------------------
    /**
     * This method initializes Terraform from the command line.
     * The static main method verifies the command line arguments and terminates if they are incorrect.
     * @param args
     * @throws IOException
     * @throws XmlParsingException
     * @throws CredentialsException
     * @throws RestorationException
     * @throws DestructionException
     * @throws CreationException
     */
    static public void main(String[] args)
    throws IOException, XmlParsingException, CredentialsException, CreationException, DestructionException, RestorationException {
        File inputXmlFile = null;
        File creds = null;
        List<String> unparsedArgs = new ArrayList<String>();
        String command = null;

        if (args != null && args.length >= 3) {
            if (!AllowedCommands.contains(args[0])) {
                String msg = "Invalid first argument: "+args[0];
                log.fatal(msg);
                throw new IOException(msg);
            }
            else {
                command = args[0].toLowerCase();
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

        // check to make sure we have legit args
        if (inputXmlFile == null) {
            String msg = "No input xml file specified!";
            throw new IOException(msg);
        }
        if (creds == null) {
            String msg = "No credentials file specified!";
            throw new IOException(msg);
        }

        Main myMain = new Main(command, inputXmlFile, creds, unparsedArgs);
        myMain.execute();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Creates a file and runs checks on it
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    static private File createFile(String filePath)
    throws FileNotFoundException {
        File result = null;

        if (!"".equals(filePath)) {
        result = new File(filePath);
            if (result.exists()) {
                if (result.isFile()) {
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
    private String command;
    private File inputXmlFile;
    private File outputXmlFile;
    private File credsFile;
    private List<Property> props;

    //----------------------------------------------------------------------------------------------
    private Main(String command, File inputXmlFile, File credsFile, List<String> unparsed) {
        this.command = command;
        startCredsParser();
        this.credsFile = credsFile;
        this.inputXmlFile = inputXmlFile;
        props = new ArrayList<Property>();
        if (unparsed != null) {
            for (String prop : unparsed) {
                try {
                    props.add(parseProperty(prop));
                }
                catch (IOException e) {
                    log.error("Unable to parse property: " + prop);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Initializes Terraform so it can execute the given commands.
     * Here is the order of operations:
     * Parses the credentials file and verifies the given credentials.
     * Generates a random string for this environment, which is appended to the output xml file.
     * Parses the xml file.
     * Runs the specified command (create, destroy, etc).
     * @throws XmlParsingException
     * @throws IOException
     * @throws CredentialsException
     * @throws CreationException
     * @throws DestructionException
     * @throws RestorationException
     */
    public void execute()
    throws XmlParsingException, IOException, CredentialsException, CreationException, DestructionException, RestorationException {
        TerraformContext context = null;
        try {
            // parse xml and set context
            context = parseContext(inputXmlFile);

            Credentials credentials = parseCredentials(credsFile);

            context.setCredentials(credentials);
            if (AllowedCommands.CREATE.getCommandName().equalsIgnoreCase(command)) {
                // create new file if creating a new environment
                UUID uuid = UUID.randomUUID();
                //convert uuid to base 62 (allowed chars: 0-9 a-z A-Z)
                ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
                bb.putLong(uuid.getMostSignificantBits());
                bb.putLong(uuid.getLeastSignificantBits());
                String suffix = Base64.encodeBase64URLSafeString(bb.array());
                suffix = suffix.replaceAll("-", "Y");
                suffix = suffix.replaceAll("_", "Z");
                suffix = suffix.substring(0, 4);
                if (context.getEnvironment() != null) {
                    context.getEnvironment().addSuffixToEnvName(suffix);
                    log.debug("UUID for env " + context.getEnvironment().getName() + " is " + suffix);
                }
                else {
                    throw new NullPointerException("No environment on context!");
                }

                String name = context.getEnvironment().getName();
                log.debug("Output filename = " + name);
                outputXmlFile = new File("env-" + name + ".xml");

                log.debug("Calling create() on context");
                context.create();
            }
            else if (AllowedCommands.DESTROY.getCommandName().equalsIgnoreCase(command)) {
                String suffix = parseSuffix(context.getEnvironment().getName());
                context.getEnvironment().setSuffix(suffix);
                log.debug("found suffix " + suffix);
                // write out instance failure regardless of success or failure
                outputXmlFile = inputXmlFile;
                log.debug("Calling destroy() on context");
                context.destroy();
            }
            else if (AllowedCommands.SUSPEND.getCommandName().equalsIgnoreCase(command)) {
                outputXmlFile = inputXmlFile;
                log.debug("Calling restore() on context");
                log.info("Attempting to suspend power on all instances/VMs in the environment.");
                context.restore();
                if (context instanceof ContextVmware) {
                    SuspendCommand newCommand = new SuspendCommand((ContextVmware) context);
                    newCommand.execute();
                }
                else if (context instanceof ContextAWS) {
                    com.urbancode.terraform.commands.aws.SuspendCommand newCommand =
                            new com.urbancode.terraform.commands.aws.SuspendCommand((ContextAWS) context);
                    newCommand.execute();
                }
                else {
                    log.warn("Could not resolve context to call command \"" + command + "\"");
                }
            }
            else if (AllowedCommands.RESUME.getCommandName().equalsIgnoreCase(command)) {
                outputXmlFile = inputXmlFile;
                log.debug("Calling restore() on context");
                context.restore();
                log.info("Attempting to power on all instances/VMs in the environment.");
                if (context instanceof ContextVmware) {
                    ResumeCommand newCommand = new ResumeCommand((ContextVmware) context);
                    newCommand.execute();
                }
                else if (context instanceof ContextAWS) {
                    com.urbancode.terraform.commands.aws.ResumeCommand newCommand =
                            new com.urbancode.terraform.commands.aws.ResumeCommand((ContextAWS) context);
                    newCommand.execute();
                }

                else {
                    log.warn("Could not resolve context to call command \"" + command + "\"");
                }
            }
            else if (AllowedCommands.TAKE_SNAPSHOT.getCommandName().equalsIgnoreCase(command)) {
                outputXmlFile = inputXmlFile;
                log.debug("Calling restore() on context");
                context.restore();
                log.info("Attempting to take snapshots of all instances/VMs in the environment.");
                if (context instanceof ContextVmware) {
                    TakeSnapshotCommand newCommand = new TakeSnapshotCommand((ContextVmware) context);
                    newCommand.execute();
                }
                else if (context instanceof ContextAWS) {
                    log.warn("Taking snapshots is not currently supported with Terraform and AWS.");
                }

                else {
                    log.warn("Could not resolve context to call command \"" + command + "\"");
                }
            }
        }
        catch (ParserConfigurationException e1) {
            throw new XmlParsingException("ParserConfigurationException: " + e1.getMessage(), e1);
        }
        catch (SAXException e2) {
            throw new XmlParsingException("SAXException: " + e2.getMessage(), e2);
        }
        finally {
            if (context != null && context.doWriteContext() && outputXmlFile != null) {
                log.debug("Writing context out to " + outputXmlFile);
                writeEnvToXml(outputXmlFile, context);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private PropertyResolver createResolver() {
        return new PropertyResolver(props);
    }

    //----------------------------------------------------------------------------------------------
    private Credentials parseCredentials(File credsFile) {
        Credentials result = null;

        Properties credProps = loadPropertiesFromFile(credsFile);
        
        for (Property cmdlineProp : props) {
            if (credProps.get(cmdlineProp.getName()) == null) {
                credProps.put(cmdlineProp.getName(), cmdlineProp.getValue());
            }
        }
        
        result = parseCredsFromProps(credProps);

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
    private TerraformContext parseContext(File xmlFileToRead)
    throws ParserConfigurationException, XmlParsingException, SAXException, IOException {
        TerraformContext result = null;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFileToRead);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();

        XmlModelParser parser = new XmlModelParser();
        parser.setPropertyResolver(createResolver());
        result = (TerraformContext) parser.parse(rootElement);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private Property parseProperty(String arg)
    throws IOException {
        String[] args;
        Property result = null;
        if (arg != null && arg.contains("=") && (args = arg.split("=")).length == 2) {
            result = new Property(args[0], args[1]);
        }
        else {
            log.error("bad property! Check your format. \nFound: " + arg);
            throw new IOException("bad parameter format");
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void writeEnvToXml(File file, TerraformContext context)
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
        CredentialsParserRegistry.getInstance().register(CredentialsMicrosoft.class.getName(), CredentialsParserMicrosoft.class);
        CredentialsParserRegistry.getInstance().register(CredentialsRackspace.class.getName(), CredentialsParserRackspace.class);
        CredentialsParserRegistry.getInstance().register(CredentialsVCloud.class.getName(), CredentialsParserVCloud.class);
    }

    //----------------------------------------------------------------------------------------------
    private String parseSuffix(String envName) {
        String result = null;
        try {
            String suffix = envName.substring(envName.length()-4);
            if (matchesBase62(suffix)) {
                result = suffix;
            }
        }
        catch (IndexOutOfBoundsException e) {
            //swallow
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private boolean matchesBase62(String s) {
        return s.matches("\\A\\b[0-9a-zA-Z]+\\b\\Z");
    }
}


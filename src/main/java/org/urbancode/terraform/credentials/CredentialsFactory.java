package org.urbancode.terraform.credentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.urbancode.commons.util.IO;

public class CredentialsFactory {


    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final Logger log = Logger.getLogger(CredentialsFactory.class);

    private static CredentialsFactory instance = new CredentialsFactory();

    static protected String homeDir = System.getProperty("com.urbancode.uprovision.storage.dir")
            + File.separator + "var" + File.separator;

    //----------------------------------------------------------------------------------------------
    public static CredentialsFactory getInstance() {
        return instance;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    protected String configDir;
    protected File configFile;

    //----------------------------------------------------------------------------------------------
    private CredentialsFactory() {
    }

    //----------------------------------------------------------------------------------------------
    public Credentials restoreForName(String name) {
        File objectFile = new File(configFile, name);
        return restoreFromFile(objectFile);
    }

    //----------------------------------------------------------------------------------------------
    public Credentials restoreFromFile(File file) {
        Credentials result = null;

        Properties props = loadPropertiesFromFile(file);

        result = parseCredsFromProps(props);

        if (result != null) {
            log.info("Restored Credentials: " + file + " : " + result.getName());
        }
        else {
            log.info("Did not restore Credentials for " + file);
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
    private Properties loadPropertiesFromFile(File file) {
        Properties result = new Properties();

//        File objectFile = new File(dir, name);
        String path = file.getAbsolutePath();

        InputStream in = null;
        try {
            in = new FileInputStream(file);
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
    public List<Credentials> getAll() {
        List<Credentials> result = new ArrayList<Credentials>();
        for (String name : restoreAllNames()) {
            result.add(restoreForName(name));
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void save(Credentials creds, String name)
    throws Exception {
        File objectFile = new File(configFile, name);
        File backup = new File(objectFile.getAbsolutePath()+".back");

        if (objectFile.exists()) {
            //move it to create a new one
            objectFile.renameTo(backup);
        }

        ObjectOutputStream oos = null;
        try {
            OutputStream fos = new FileOutputStream(objectFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(creds);
        }
        catch (Exception e) {
            log.error("Error saving object to file : " + objectFile.getAbsolutePath(), e);
            backup.renameTo(objectFile);
            creds = null;
        }
        finally {
            IO.close(oos);
        }

        if (backup.exists()) {
            backup.delete();
        }
    }

    //----------------------------------------------------------------------------------------------
    public List<String> restoreAllNames() {
        List<String> names = new ArrayList<String>();
        log.debug("looking in directory: " + configFile);
        for (File file : IO.listFilesRecursive(configFile)) {
            String fname = file.getName();
            int suffixIndex = fname.indexOf(".xml");
            if(suffixIndex > -1) {
                names.add(fname.substring(0, suffixIndex));
            }
            else {
                names.add(fname);
            }
        }

        return names;
    }

    //----------------------------------------------------------------------------------------------
    public void delete(String name) throws FileNotFoundException {
        File objectFile = new File(configFile, name);
        if (objectFile.exists()) {
            objectFile.delete();
        }
        else {
            throw new FileNotFoundException("Object with name " + name + " not found!");
        }
    }

    //----------------------------------------------------------------------------------------------
    public List<Credentials> restoreAll()
    throws Exception {
        List<Credentials> result = new ArrayList<Credentials>();
        for (String name : restoreAllNames()) {
            result.add(restoreForName(name));
        }

        return result;
    }
}

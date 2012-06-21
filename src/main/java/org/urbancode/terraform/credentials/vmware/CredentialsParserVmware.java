package org.urbancode.terraform.credentials.vmware;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;
import org.urbancode.terraform.tasks.vmware.util.Path;


public class CredentialsParserVmware extends CredentialsParser {

    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    static private final Logger log = Logger.getLogger(CredentialsParserVmware.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials parse(Properties props) {
        Credentials result = null;
        
        String name = props.getProperty("name");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        
        String url = props.getProperty("url");
        
        // parse list of hosts
        String hosts = props.getProperty("hosts");
        
        List<Path> hostPathList = new ArrayList<Path>();
        if (hosts != null && !hosts.equals("")) {
            // split the hosts by a comma, add them to list
            String[] splitHosts = StringUtils.split(hosts, ',');
            splitHosts = StringUtils.stripAll(splitHosts);
               for (String host : splitHosts) {
                   hostPathList.add(new Path(host));
               }
        }
        result = new CredentialsVmware(name, username, password, url, hostPathList);
        
        return result;
    }
    
}

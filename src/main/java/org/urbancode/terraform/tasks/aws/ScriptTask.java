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
package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;


public class ScriptTask extends BootActionSubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(ScriptTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String url;     // url where to grab the script
    private String shell;   // the shell that the script will be ran in
    // list of parameters to be passed to the script
    private List<ParamTask> params = new ArrayList<ParamTask>();
    
    // This is the shell that makes up the User-Data, not the shell to run the script in.
    // This is used for determining how to check for a connection and grab the script.
    private String runInShell;
    
    //----------------------------------------------------------------------------------------------
    public ScriptTask(ContextAWS context) {
        this.context = context;
    }
    
    public ParamTask createParam() {
        ParamTask param = new ParamTask(context);
        params.add(param);
        return param;
    }
    
    public List<ParamTask> getParams() {
        return Collections.unmodifiableList(params);
    }
    
    //----------------------------------------------------------------------------------------------
    public String getShell() {
        return shell;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getUrl() {
        return url;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setShell(String shell) {
        this.shell = shell;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setUrl(String url) {
        this.url =  url;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setRunInShell(String runInShell) {
        this.runInShell = runInShell;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCmds(String script) {
        this.script = script;
        
    }

    //----------------------------------------------------------------------------------------------
    @Override
    protected String getCmds() {
        return script;
    }
    
    //----------------------------------------------------------------------------------------------
    public void create() {
        String taskData = "";
        
        taskData += getShell() + " `wget " + getUrl() + "`";
        
        if (getParams() != null && !getParams().isEmpty()) {
            for (ParamTask param : getParams()) {
                taskData += " " + param.getValue();
            }
        }
        taskData += "; \n";
        
        setCmds(taskData);
    }
    
    //----------------------------------------------------------------------------------------------
    public void destroy() {
        setCmds(null);
    }

}

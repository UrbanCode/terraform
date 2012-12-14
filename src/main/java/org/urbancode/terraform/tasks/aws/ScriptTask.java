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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;


public class ScriptTask extends BootActionSubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(ScriptTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private ContextAWS context;

    //url to grab the script and shell the script will be run in
    private String url;
    private String shell;
    // list of parameters to be passed to the script
    private List<ParamTask> params = new ArrayList<ParamTask>();

    //----------------------------------------------------------------------------------------------
    public ScriptTask(ContextAWS context) {
        super(context);
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
    @Override
    public void create() {
        String cmds = "";
        File tmp = null;
        try {
            tmp = File.createTempFile("tf.init.", ".sh");
        }
        catch (IOException e) {
            throw new RuntimeException("Could not create temp file for init scripts", e);
        }
        String scriptName = tmp.getName();
        String wgetCmd = "wget -O " + scriptName + " " + getUrl() + "; ";
        String shellCmd = "/bin/bash " + scriptName;

        for (ParamTask param : getParams()) {
            shellCmd += " " + param.getValue();
        }
        shellCmd += "; ";
        cmds = wgetCmd + "\n" + shellCmd;

        setCmds(cmds);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        setCmds(null);
    }

}

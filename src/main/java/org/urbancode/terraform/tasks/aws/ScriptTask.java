package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;


public class ScriptTask extends PostCreateSubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(ScriptTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private ContextAWS context;
    
    private String url;
    private String shell;
    private List<ParamTask> params;
    
    //----------------------------------------------------------------------------------------------
    public ScriptTask(ContextAWS context) {
        this.context = context;
        params = new ArrayList<ParamTask>();
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
    public void create() {
        
        String userData = "";
        userData += "curl " + getUrl() + " | bash -s ";
        if (getParams() != null && !getParams().isEmpty()) {
            for (ParamTask param : getParams()) {
                userData += " " + param.getValue();
            }
        }
        userData += "; \n";
        
        setCmds(userData);
    }
    
    //----------------------------------------------------------------------------------------------
    public void destroy() {
        setCmds(null);
    }

}

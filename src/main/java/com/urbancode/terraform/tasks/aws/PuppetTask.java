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
package com.urbancode.terraform.tasks.aws;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;


public class PuppetTask extends BootActionSubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PuppetTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String name;
    private String manifestUrl;
    private String destPath;
    private String workingDir;
    private boolean isModule;
    private File postCreateScript;

    //----------------------------------------------------------------------------------------------
    public PuppetTask(ContextAWS context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    public void setModule(boolean module) {
        this.isModule = module;
    }

    //----------------------------------------------------------------------------------------------
    public boolean getModule() {
        return isModule;
    }

    //----------------------------------------------------------------------------------------------
    public void setScript(File script) {
        this.postCreateScript = script;
    }

    //----------------------------------------------------------------------------------------------
    public File findScript() {
        return this.postCreateScript;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public void setManifestUrl(String url) {
        this.manifestUrl = url;
    }

    //----------------------------------------------------------------------------------------------
    public String getManifestUrl() {
        return manifestUrl;
    }

    //----------------------------------------------------------------------------------------------
    public void setDestPath(String path) {
        this.destPath = path;
    }

    //----------------------------------------------------------------------------------------------
    public String getDestPath() {
        return destPath;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        workingDir = "/tmp";
        FileWriter fw = null;
        try {
            fw = new FileWriter(findScript(), true);

            fw.write("\n\n");
            fw.write("MANIFEST_URL=\"" + getManifestUrl() + "\"\n");
            fw.write("FILE_NAME=\"" + getName() + "\"\n");
            fw.write("DEST_PATH=\"" + getDestPath() + "\"\n");
            fw.write("WORK_DIR=\"" + workingDir + "\"\n\n");

            String setDir   = "cd $WORK_DIR; "            + "\n" +
                              "wget -t 45 $MANIFEST_URL; " + "\n" +
                              "mkdir -p $DEST_PATH; "     + "\n";
            fw.write(setDir);

            if (getModule()) {
                fw.write("mv $FILE_NAME.tar.gz $DEST_PATH" + "\n ");
                fw.write("cd $DEST_PATH" + "\n");
                fw.write("tar xvf $DEST_PATH/$FILE_NAME.tar.gz" + "\n");
                fw.write("cd $WORK_DIR" + "\n");
                fw.write("puppet $DEST_PATH/$FILE_NAME/manifests/init.pp >> /home/ubuntu/puppetLOG.out" + "\n");
            }
            else {
                fw.write("mv $FILE_NAME $DEST_PATH" + "\n");
                fw.write("puppet $DEST_PATH/$FILE_NAME" + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        //nothing to destroy
    }

    @Override
    public void setCmds(String script) {
        this.script = script;
    }

    @Override
    protected String getCmds() {
        return script;
    }

}

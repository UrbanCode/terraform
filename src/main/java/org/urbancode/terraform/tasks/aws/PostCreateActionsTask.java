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
import org.urbancode.terraform.tasks.PostCreateException;
import org.urbancode.terraform.tasks.common.TerraformContext;

import com.urbancode.x2o.tasks.SubTask;


public class PostCreateActionsTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PostCreateActionsTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private List<PostCreateActionTask> actions = new ArrayList<PostCreateActionTask>();
    private String host;
    private String idFilePath;

    //----------------------------------------------------------------------------------------------
    public PostCreateActionsTask(TerraformContext context) {
        super(context);
    }

    //----------------------------------------------------------------------------------------------
    public void setHost(String host) {
        this.host = host;
    }

    //----------------------------------------------------------------------------------------------
    public void setIdFile(String idFilePath) {
        this.idFilePath = idFilePath;
    }

    //----------------------------------------------------------------------------------------------
    public List<PostCreateActionTask> getPostCreateActions() {
        return Collections.unmodifiableList(actions);
    }

    //----------------------------------------------------------------------------------------------
    public SshTask createSsh() {
        SshTask tmp = new SshTask((TerraformContext) context);
        actions.add(tmp);
        return tmp;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create()
    throws PostCreateException {
        log.debug("Attempting to run Post Create Actions");
        if (actions != null) {
            for (PostCreateActionTask action : actions) {
                if (idFilePath != null) {
                    action.setIdFilePath(idFilePath);
                }

                if (host != null) {
                    action.setHost(host);
                    try {
                        action.create();
                    }
                    catch (Exception e) {
                        log.error("Could not complete Post Create Action completely", e);
                        throw new PostCreateException("Failed Post Create Action", e);
                    }
                }
                else {
                    log.error("Host is null!" +
                              "\nNo where to connect to do PostCreateActions!" +
                              "\nSkipping PCAs");
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy()
    throws PostCreateException {

    }

}

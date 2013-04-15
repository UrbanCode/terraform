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
package com.urbancode.terraform.tasks.vmware.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Path implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    private static final long serialVersionUID = 1L;
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private List<String> path;

    //----------------------------------------------------------------------------------------------
    public Path(Path parent, String name) {
        List<String> path = parent.toList();
        String[] split = name.split("/");
        for (int i=0; i<split.length; i++) {
            path.add(split[i]);
        }
        this.path = path;
    }

    //----------------------------------------------------------------------------------------------
    public Path(String path) {
        // scrub path
        path = path.replaceAll("/+", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        this.path = Arrays.asList(path.split("/"));
    }

    //----------------------------------------------------------------------------------------------
    private Path(List<String> path) {
        this.path = path;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String p : path) {
            sb.append("/");
            sb.append(p);
        }
        return sb.toString();
    }

    //----------------------------------------------------------------------------------------------
    public Path getFolders() {
        Path result;
        if (path.size() < 1) {
            throw new RuntimeException("No folders specified");
        }
        else {
            result = new Path(path.subList(1, path.size()));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getDatacenterName() {
        String result;
        if (path.size() < 1) {
            throw new RuntimeException("No datacenter specified");
        }
        else {
            result = path.get(0);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getHostName() {
        String result;
        if (path.size() < 2) {
            throw new RuntimeException("No host specified");
        }
        else {
            result = path.get(1);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        String result;
        if (path.size() == 0) {
            result = "";
        }
        else {
            result = path.get(path.size() - 1);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Path getParent() {
        Path result;
        if (path.size() == 0) {
            result = new Path(Collections.<String>emptyList());
        }
        else {
            result = new Path(path.subList(0, path.size() - 1));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Path getParentFolders() {
        Path result;
        if (path.size() < 2) {
            throw new RuntimeException("No parent folders");
        }
        else {
            result = new Path(path.subList(1, path.size() - 1));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public List<String> toList() {
        return new ArrayList<String>(path);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object otherPath) {
        boolean result;
        if (otherPath instanceof Path) {
            result = this.toString().equals(otherPath.toString());
        }
        else {
            result = false;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

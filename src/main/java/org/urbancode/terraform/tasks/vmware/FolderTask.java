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
package org.urbancode.terraform.tasks.vmware;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.vmware.util.Path;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.urbancode.x2o.tasks.Restorable;
import com.urbancode.x2o.tasks.RestorationException;
import com.urbancode.x2o.tasks.SubTask;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VimFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ManagedEntity;

public class FolderTask extends SubTask implements Restorable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(FolderTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private Folder parentFolder = null;
    private Folder folder = null;
    private String folderName = null;
    private Path folderRef = null;
    private Path destPath = null;
    private VirtualHost host = null;

    //----------------------------------------------------------------------------------------------
    public FolderTask() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public String getFolderName() {
        return this.folderName;
    }

    //----------------------------------------------------------------------------------------------
    public Path getFolderRef() {
        return this.folderRef;
    }

    //----------------------------------------------------------------------------------------------
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    //----------------------------------------------------------------------------------------------
    public void setDestPath(Path destPath) {
        this.destPath = destPath;
    }

    //----------------------------------------------------------------------------------------------
    public void setVirtualHost(VirtualHost host) {
        this.host = host;
    }


    //----------------------------------------------------------------------------------------------
    /**
     * Creates a folder in vCenter. Folders must have unique names. Terraform will automatically
     * append a hopefully unique UUID to the folder name.
     */
    @Override
    public void create() {
        try {
            this.parentFolder = getFolderFromDatacenter(destPath);
            folder = parentFolder.createFolder(folderName);
            folderRef = new Path(destPath, folderName);
        }
        catch (InvalidName e) {
            log.fatal("Invalid folder name", e);
        }
        catch (DuplicateName e) {
            log.fatal("Folder name was a duplicate; duplicates not allowed", e);
        }
        catch (RuntimeFault e) {
            log.fatal("Unknown runtime fault during folder creation", e);
        }
        catch (RemoteException e) {
            log.fatal("Remote exception during folder creation", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        try {
            folder.destroy_Task();
        }
        catch (VimFault e) {
            log.fatal("Vim failed during folder destruction", e);
        }
        catch (RuntimeFault e) {
            log.fatal("RuntimeFault during folder destruction", e);
        }
        catch (RemoteException e) {
            log.fatal("RemoteException during folder destruction", e);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void restore() throws RestorationException {
        this.folderRef = new Path(destPath, folderName);
        log.debug("restoring folder " + this.folderRef.toString());
        try {
            this.folder = getFolderFromDatacenter(folderRef);
        } catch (RemoteException e) {
            throw new RestorationException(e);
        }
    }

    //----------------------------------------------------------------------------------------------
    public Folder getFolder() {
        return this.folder;
    }

    //----------------------------------------------------------------------------------------------
    public Folder getParentFolder() {
        return this.parentFolder;
    }

    //----------------------------------------------------------------------------------------------
    public Folder getFolderFromDatacenter(Path path)
    throws RemoteException {
        Folder result = null;
        log.debug("dest path: " + path.toString());
        List<String> folderNames = path.getFolders().toList();
        Datacenter datacenter = host.getDatacenter(path);

        // traverse folders
        result = datacenter.getVmFolder();
        Folder nextFolder = null;
        for (String fName : folderNames) {
            for (ManagedEntity e : result.getChildEntity()) {
                if (e instanceof Folder && e.getName().equals(fName)) {
                    nextFolder = (Folder) e;
                    break;
                }
            }
            if (nextFolder == null) {
                throw new NotFound();
            }
            result = nextFolder;
            nextFolder = null;
        }
        if (result == null) {
            throw new NotFound();
        }

        return result;
    }

}

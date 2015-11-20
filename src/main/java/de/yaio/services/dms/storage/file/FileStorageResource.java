/** 
 * software for document-storage
 * 
 * @FeatureDomain                DMS
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     DMS
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.services.dms.storage.file;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.yaio.services.dms.storage.StorageResource;
import de.yaio.services.dms.storage.StorageResourceVersion;

/** 
 * implementation of the StorageResource
 * 
 * @FeatureDomain                document-storage
 * @package                      de.yaio.services.dms.storage.file
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     document-storage
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class FileStorageResource implements StorageResource {
    protected String id;
    protected Integer curVersion;
    protected Date created;
    protected Date lastChanged;
    protected Map<Integer, StorageResourceVersion> versions = new HashMap<Integer , StorageResourceVersion>();

    public FileStorageResource() {
    }
    public FileStorageResource(String id, Integer curVersion, Date created,
                                Date lastChanged, HashMap<Integer , StorageResourceVersion> versions) {
        super();
        this.id = id;
        this.curVersion = curVersion;
        this.created = created;
        this.lastChanged = lastChanged;
        if (versions != null) {
            this.versions = versions;
        }
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCurVersion() {
        return this.curVersion;
    }

    public void setCurVersion(Integer curVersion) {
        this.curVersion = curVersion;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastChanged() {
        return this.lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public Map<Integer, StorageResourceVersion> getVersions() {
        return this.versions;
    }

    public void setVersions(Map<Integer, StorageResourceVersion> resourceVersions) {
        this.versions = resourceVersions;
    }

    public StorageResourceVersion getVersion(Integer version) {
        return this.versions.get(version);
    }
}

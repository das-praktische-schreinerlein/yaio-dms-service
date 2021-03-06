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
package de.yaio.services.dms.api.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
public class StorageResourceImpl implements StorageResource {
    protected String dmsId;
    protected String srcId;
    protected Integer curVersion;
    protected Date created;
    protected Date lastChanged;
    protected Map<Integer, StorageResourceVersion> versions = new HashMap<Integer , StorageResourceVersion>();

    public StorageResourceImpl() {
    }
    public StorageResourceImpl(final String dmsId, final String srcId, final Integer curVersion, final Date created,
                               final Date lastChanged, final HashMap<Integer, StorageResourceVersion> versions) {
        super();
        this.dmsId = dmsId;
        this.srcId = srcId;
        this.curVersion = curVersion;
        this.created = created;
        this.lastChanged = lastChanged;
        if (versions != null) {
            this.versions = versions;
        }
    }

    public String getDMSId() {
        return this.dmsId;
    }

    public void setDMSId(final String id) {
        this.dmsId = id;
    }

    public String getSrcId() {
        return this.srcId;
    }

    public void setSrcId(final String srcId) {
        this.srcId = srcId;
    }

    public Integer getCurVersion() {
        return this.curVersion;
    }

    public void setCurVersion(final Integer curVersion) {
        this.curVersion = curVersion;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getLastChanged() {
        return this.lastChanged;
    }

    public void setLastChanged(final Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public Map<Integer, StorageResourceVersion> getVersions() {
        return this.versions;
    }

    public void setVersions(final Map<Integer, StorageResourceVersion> resourceVersions) {
        this.versions = resourceVersions;
    }

    public StorageResourceVersion getVersion(final Integer version) {
        return this.versions.get(version);
    }
}

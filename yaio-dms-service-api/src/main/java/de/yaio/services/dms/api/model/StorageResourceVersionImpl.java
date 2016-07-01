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

/** 
 * implementation of the StorageResourceVersion
 * 
 * @FeatureDomain                document-storage
 * @package                      de.yaio.services.dms.storage.file
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     document-storage
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class StorageResourceVersionImpl implements StorageResourceVersion {
    public Integer version;
    public String origName;
    public String resName;
    public Date created;

    public StorageResourceVersionImpl() {

    }
    public StorageResourceVersionImpl(Integer version, String origName, String resName, Date created) {
        super();
        this.version = version;
        this.origName = origName;
        this.resName = resName;
        this.created = created;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOrigName() {
        return this.origName;
    }

    public void setOrigName(String origName) {
        this.origName = origName;
    }

    public String getResName() {
        return this.resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}

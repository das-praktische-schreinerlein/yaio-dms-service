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
package de.yaio.services.dms.storage;

import java.util.Date;
import java.util.Map;


/** 
 * a stored resource
 * 
 * @FeatureDomain                document-management
 * @package                      de.yaio.dms
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     collaboration
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public interface StorageResource {
    public String getId();
    public Integer getCurVersion();
    public Date getCreated();
    public Date getLastChanged();
    public Map<Integer , StorageResourceVersion> getVersions();

    public void setId(String id);
    public void setCurVersion(Integer version);
    public void setCreated(Date created);
    public void setLastChanged(Date lastChanged);
    public void setVersions(Map<Integer , StorageResourceVersion> versions);

    public StorageResourceVersion getVersion(Integer version);
}

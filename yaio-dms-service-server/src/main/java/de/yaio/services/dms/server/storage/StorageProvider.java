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
package de.yaio.services.dms.server.storage;

import de.yaio.commons.io.IOExceptionWithCause;
import de.yaio.services.dms.api.model.StorageResource;
import de.yaio.services.dms.api.model.StorageResourceVersion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/** 
 * provider-interface to the dms-implementation 
 * 
 * @FeatureDomain                document-management
 * @package                      de.yaio.dms
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     collaboration
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public interface StorageProvider {

    /**
     * create resource with that file
     * @param appId                  the appId of the store
     * @param srcId                  srcId from client (based on that value the dmsId will be generated)
     * @param origName               original name
     * @param uploadFile             inputstream with the data
     * @return                       returns the metadata of the target resource
     * @throws IOExceptionWithCause  already exists
     * @throws IOException           errors while copying
     */
    public StorageResource add(String appId, String srcId, String origName, InputStream uploadFile)
            throws IOExceptionWithCause, IOException;
    
    /**
     * update the requested resource with that file
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @param origName               original name
     * @param uploadFile             inputstream with the data
     * @return                       returns the metadata of the target resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while copying
     */
    public StorageResource update(String appId, String dmsId, String origName, InputStream uploadFile)
            throws IOExceptionWithCause, IOException;
    
    /**
     * reset the requested resource to to this version
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @param targetVersion          version of the resource
     * @return                       returns the metadata of the target resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while deleting
     */
    public StorageResource resetToVersion(String appId, String dmsId, Integer targetVersion)
            throws IOExceptionWithCause, IOException;
    
    /**
     * delete all data of the requested resource
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while deleting
     */
    public void delete(String appId, String dmsId) throws IOExceptionWithCause, IOException;
    
    /**
     * returns metadata of the requested resource
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @return                       returns the metadata of the requested resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    public StorageResource getMetaData(String appId, String dmsId) throws IOExceptionWithCause, IOException;
    
    /**
     * returns metadata of the requested resource-version
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @param requestedVersion       version of the resource
     * @return                       returns the metadata of the requested resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    public StorageResourceVersion getMetaData(String appId, String dmsId, Integer requestedVersion)
            throws IOExceptionWithCause, IOException;
    
    /**
     * returns the path of the requested resource
     * @param appId                  the appId of the store
     * @param dmsId                  dmsId of the resource
     * @param requestedVersion       version of the resource
     * @return                       returns the path of the requested resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    public Path getResource(String appId, String dmsId, Integer requestedVersion)
            throws IOExceptionWithCause, IOException;
}
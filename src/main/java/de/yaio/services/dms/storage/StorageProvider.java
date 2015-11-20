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
     * @param id                     id of the resource
     * @param origName               original name
     * @param uploadFile             inputstream with the data
     * @return                       returns the metadata of the target resource
     * @throws IOException           if resource not exists
     */
    public StorageResource add(String appId, String id, String origName, InputStream uploadFile) throws IOException;
    
    /**
     * update the requested resource with that file
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @param origName               original name
     * @param uploadFile             inputstream with the data
     * @return                       returns the metadata of the target resource
     * @throws IOException           if resource not exists
     */
    public StorageResource update(String appId, String id, String origName, InputStream uploadFile) throws IOException;
    
    /**
     * reset the requested resource to to this version
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @param targetVersion          version of the resource
     * @return                       returns the metadata of the target resource
     * @throws IOException           if resource not exists
     */
    public StorageResource resetToVersion(String appId, String id, Integer targetVersion) throws IOException;
    
    /**
     * delete all data of the requested resource
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @throws IOException           if resource not exists
     */
    public void delete(String appId, String id) throws IOException;
    
    /**
     * 
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @return                       returns the metadata of the requested resource
     * @throws IOException           if resource not exists
     */
    public StorageResource getMetaData(String appId, String id) throws IOException;
    
    /**
     * returns metadata of the requested resource
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @param requestedVersion       version of the resource
     * @return                       returns the metadata of the requested resource
     * @throws IOException           if resource not exists
     */
    public StorageResourceVersion getMetaData(String appId, String id, Integer requestedVersion) throws IOException;
    
    /**
     * returns the path of the requested resource
     * @param appId                  the appId of the store
     * @param id                     id of the resource
     * @param requestedVersion       version of the resource
     * @return                       returns the path of the requested resource
     * @throws IOException           if resource not exists
     */
    public Path getResource(String appId, String id, Integer requestedVersion) throws IOException;
}
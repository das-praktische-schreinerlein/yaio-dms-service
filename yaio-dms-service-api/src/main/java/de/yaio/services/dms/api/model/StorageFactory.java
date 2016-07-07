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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/** 
 * services for document-storage
 *  
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 */
public class StorageFactory {

    protected ObjectMapper storageResourceMapper;
    protected ObjectMapper storageResourceVersionMapper;

    public static StorageFactory createStorageFactory() {
        return new StorageFactory();
    }

    protected StorageFactory() {
        storageResourceMapper = createStorageResourceMapper();
        storageResourceVersionMapper = createStorageResourceVersionMapper();
    }

    /**
     * parse StorageResource from json
     * @param metaJson               json to be parsed 
     * @return                       returns the StorageResource
     * @throws IOException           if deserialization fails
     */
    public StorageResource parseStorageResourceFromJson(String metaJson) throws IOException {
        return storageResourceMapper.readValue(metaJson, StorageResourceImpl.class);
    }

    /**
     * parse StorageResourceVersion from json
     * @param metaJson               json to be parsed 
     * @return                       returns the StorageResourceVersion
     * @throws IOException           if deserialization fails
     */
    public StorageResourceVersion parseStorageResourceVersionFromJson(String metaJson) throws IOException {
        return storageResourceVersionMapper.readValue(metaJson, StorageResourceVersionImpl.class);
    }

    protected ObjectMapper createStorageResourceMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // configure
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        // add deserializer
        SimpleModule module = new SimpleModule("StorageResourceVersionDeserializer", new Version(1, 0, 0, null));
        StorageResourceVersionDeserializer storageResourceVersionDeserializer = new StorageResourceVersionDeserializer();
        StorageResourceDeserializer storageResourceDeserializer = new StorageResourceDeserializer();
        module.addDeserializer(StorageResourceVersion.class, storageResourceVersionDeserializer);
        module.addDeserializer(StorageResource.class, storageResourceDeserializer);

        mapper.registerModule(module);

        return mapper;
    }

    protected ObjectMapper createStorageResourceVersionMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // configure
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        // add deserializer
        SimpleModule module = new SimpleModule("StorageResourceVersionDeserializer", new Version(1, 0, 0, null));
        StorageResourceVersionDeserializer storageResourceVersionDeserializer = new StorageResourceVersionDeserializer();
        module.addDeserializer(StorageResourceVersion.class, storageResourceVersionDeserializer);

        mapper.registerModule(module);

        return mapper;
    }
}

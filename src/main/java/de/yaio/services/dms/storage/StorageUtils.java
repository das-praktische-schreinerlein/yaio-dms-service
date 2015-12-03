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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.yaio.services.dms.storage.file.FileStorageResource;
import de.yaio.services.dms.storage.file.FileStorageResourceVersion;

/** 
 * services for document-storage
 *  
 * @FeatureDomain                service
 * @package                      de.yaio.services.dms.controller
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     DMS
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
@Service
public class StorageUtils {
    // Jackson
    @Autowired
    protected ObjectMapper mapper;
    
    /**
     * normalize the filename
     * @param fileName               fileName to be normalized
     * @return                       returns the fileName
     */
    public String normalizeFileName(String fileName) {
        String newFileName = fileName;
        // replace all . but the last as extension
        newFileName = fileName.replaceAll("\\.(?=.*\\.)", "_");
        
        // replace all not matching characters
        newFileName = fileName.replaceAll("[^a-zA-Z0-9-.]", "_");
        return newFileName;
    }

    /**
     * serialize the resource as json
     * @param resource               resource to be serialized as json
     * @return                       returns the json
     * @throws IOException           if serialization fails
     */
    public String serializeMetaDataToJson(StorageResource resource) throws IOException {
        ObjectWriter writer = mapper.writer();
        String metaJson = writer.writeValueAsString(resource);
        return metaJson;
    }

    /**
     * parse StorageResource from json
     * @param metaJson               json to be parsed 
     * @return                       returns the StorageResource
     * @throws IOException           if deserialization fails
     */
    public StorageResource parseStorageResourceFromJson(String metaJson) throws IOException {
        // configure
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        // add deserializer
        SimpleModule module = new SimpleModule("StorageResourceVersionDeserializer", new Version(1, 0, 0, null));
        StorageResourceVersionDeserializer storageResourceVersionDeserializer = new StorageResourceVersionDeserializer();
        StorageResourceDeserializer fileStorageResourceDeserializer = new StorageResourceDeserializer();
        module.addDeserializer(StorageResourceVersion.class, storageResourceVersionDeserializer);
        module.addDeserializer(StorageResource.class, fileStorageResourceDeserializer);

        mapper.registerModule(module);
        StorageResource resource = mapper.readValue(metaJson, FileStorageResource.class);
        return resource;
    }

    /**
     * parse StorageResourceVersion from json
     * @param metaJson               json to be parsed 
     * @return                       returns the StorageResourceVersion
     * @throws IOException           if deserialization fails
     */
    public StorageResourceVersion parseStorageResourceVersionFromJson(String metaJson) throws IOException {
        // configure
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        // add deserializer
        SimpleModule module = new SimpleModule("StorageResourceVersionDeserializer", new Version(1, 0, 0, null));
        StorageResourceVersionDeserializer storageResourceVersionDeserializer = new StorageResourceVersionDeserializer();
        module.addDeserializer(StorageResourceVersion.class, storageResourceVersionDeserializer);

        mapper.registerModule(module);
        StorageResourceVersion resource = mapper.readValue(metaJson, FileStorageResourceVersion.class);
        return resource;
    }
}

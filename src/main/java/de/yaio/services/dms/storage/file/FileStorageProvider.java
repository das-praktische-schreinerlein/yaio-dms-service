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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.yaio.services.dms.storage.StorageProvider;
import de.yaio.services.dms.storage.StorageResource;
import de.yaio.services.dms.storage.StorageResourceVersion;

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
public class FileStorageProvider implements StorageProvider {
    
    private static String VERSION_ID_FS1 = "FS1_";

    // Jackson
    @Autowired
    protected ObjectMapper mapper;
    
    @Value("${yaio-dms-service.storagebasedir}")
    protected String storageBaseDir;

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#add(java.lang.String, java.lang.String, java.io.InputStream)
     */
    @Override
    public StorageResource add(String appId, String srcId, String origName, InputStream uploadFile) throws IOException {
        // create id
        String dmsId = convertSrcIdToDMSId_FS1(srcId);

        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (dirStructure.exists()) {
            throw new IOException("error document already exists HTTP: 409 Conflict");
        }
        dirStructure.mkdirs();

        // resName
        String resName = convertToResourceFileName(origName, 1);

        // create resource
        StorageResourceVersion resourceVersion = new FileStorageResourceVersion(1, origName, resName, new Date());
        StorageResource resource = new FileStorageResource(dmsId, srcId, 1, new Date(), new Date(), null);
        resource.getVersions().put(resourceVersion.getVersion(), resourceVersion);

        // save Uploadfile
        saveResourceFile(uploadFile, dirStructure, resName);

        // save metadata
        saveMetaDataFile(resource, getMetaDataFile(dirStructure), true);

        return resource;
    }

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#update(java.lang.String, java.lang.String, java.io.InputStream)
     */
    @Override
    public StorageResource update(String appId, String dmsId, String origName, InputStream uploadFile) throws IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));

        // resName
        resource.setCurVersion(resource.getCurVersion() + 1);
        resource.setLastChanged(new Date());

        String resName = convertToResourceFileName(origName, resource.getCurVersion());
        StorageResourceVersion resourceVersion = new FileStorageResourceVersion(resource.getCurVersion(), origName, resName, new Date());
        resource.getVersions().put(resourceVersion.getVersion(), resourceVersion);

        // save Uploadfile
        saveResourceFile(uploadFile, dirStructure, resName);

        // save metadata
        saveMetaDataFile(resource, getMetaDataFile(dirStructure), false);

        return resource;
    }

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#resetToVersion(java.lang.String, java.lang.Integer)
     */
    @Override
    public StorageResource resetToVersion(String appId, String dmsId, Integer targetVersion) throws IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));

        // check targetVersion
        StorageResourceVersion resourceVersion = resource.getVersion(targetVersion);
        if (resourceVersion == null) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // remove versions
        for (StorageResourceVersion resourceVersionLoop : resource.getVersions().values()) {
            if (resourceVersionLoop.getVersion().intValue() > targetVersion.intValue()) {
                try {
                    File resFile = getResourceFile(dirStructure, resourceVersionLoop.getResName());
                    resFile.delete();
                } catch (Exception ex) {
                }
                resource.getVersions().remove(resourceVersionLoop.getVersion());
            }
        }

        // set and save new metadata
        resource.setCurVersion(targetVersion);
        saveMetaDataFile(resource, getMetaDataFile(dirStructure), false);

        return resource;
    }

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#delete(java.lang.String)
     */
    @Override
    public void delete(String appId, String dmsId) throws IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));

        // remove versions
        for (StorageResourceVersion resourceVersion : resource.getVersions().values()) {
            File res = getResourceFile(dirStructure, resourceVersion.getResName());
            res.delete();
        }

        // remove metaDataFile
        File metaDataFile = getMetaDataFile(dirStructure);
        metaDataFile.delete();

        // remove dirStructure
        dirStructure.delete();
    }

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#getMetaData(java.lang.String)
     */
    @Override
    public StorageResource getMetaData(String appId, String dmsId) throws IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));
        return resource;
    }

    /* (non-Javadoc)
     * @see de.yaio.services.filestorage.modell.StorageProvider#getMetaData(java.lang.String, java.lang.Integer)
     */
    @Override
    public StorageResourceVersion getMetaData(String appId, String dmsId, Integer requestedVersion) throws IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));
        if (requestedVersion == null || requestedVersion.intValue() == 0) {
            requestedVersion = resource.getCurVersion();
        }
        StorageResourceVersion resourceVersion = resource.getVersion(requestedVersion);
        if (resourceVersion == null) {
            throw new IOException("error document not exists HTTP: 404 Not Found");
        }

        // return resource
        return resourceVersion;
    }

    @Override
    public Path getResource(String appId, String dmsId, Integer requestedVersion) throws IOException {
        return getResourceFile(appId, dmsId, requestedVersion).toPath();
    }

    public File getResourceFile(String appId, String dmsId, Integer requestedVersion) throws IOException {
        File dirStructure = getDirStructure(appId, dmsId);
        StorageResourceVersion resourceVersion = getMetaData(appId, dmsId, requestedVersion);
        return getResourceFile(dirStructure, resourceVersion.getResName());
    }

    protected String normalizeFileName(String fileName) {
        String newFileName = fileName;
        // replace all . but the last as extension
        newFileName = fileName.replaceAll("\\.(?=.*\\.)", "_");
        
        // replace all not matching characters
        newFileName = fileName.replaceAll("[^a-zA-Z0-9-.]", "_");
        return newFileName;
    }

    protected String convertDMSIdToDirStructure(String appId, String dmsId) throws IOException {
        if (dmsId.startsWith(VERSION_ID_FS1)) {
            return convertDMSIdToDirStructure_FS1(appId, dmsId);
        }
        throw new IOException("unknwon version of dmsId:" + dmsId);
    }
    
    protected String convertDMSIdToDirStructure_FS1(String appId, String dmsId) throws IOException {
        if (!dmsId.startsWith(VERSION_ID_FS1)) {
            throw new IOException("unknwon version of dmsId:" + dmsId);
        }
        
        // set basepath
        String basePath = storageBaseDir + File.separator + normalizeFileName(appId);
        
        // normalize id and create a structure of 4 chars per dir
        String fileName = dmsId.substring(VERSION_ID_FS1.length());
        fileName = normalizeFileName(fileName);
        String[] separatedFileName = fileName.split("(?<=\\G....)");
        fileName = StringUtils.arrayToDelimitedString(separatedFileName, File.separator);
        
        return basePath + File.separator + fileName;
    }

    protected String convertToResourceFileName(String origName, int version) {
        return normalizeFileName("v" + version + "-" + origName);
    }

    protected String convertSrcIdToDMSId_FS1(String srcId) {
        return VERSION_ID_FS1 + Base64Utils.encodeToString(normalizeFileName(srcId).getBytes());
    }
    
    protected File getDirStructure(String appId, String dmsId) throws IOException {
        return new File(convertDMSIdToDirStructure(appId, dmsId));
    }
    protected File getMetaDataFile(File dirStructure) {
        return new File(dirStructure.getAbsolutePath() + File.separator + "metadata.json");
    }

    protected File getResourceFile(File dirStructure, String resName) {
        return new File(dirStructure.getAbsolutePath() + File.separator + resName);
    }
    
    protected void saveResourceFile(InputStream uploadFile, File dirStructure, String resName) throws IOException {
        Files.copy(uploadFile, getResourceFile(dirStructure, resName).toPath());
    }


    protected void saveMetaDataFile(StorageResource resource, File metadataFile, boolean create) throws IOException {
        if (create && metadataFile.exists()) {
            throw new IOException("error metadata document already exists HTTP: 409 Conflict");
        }
        ObjectWriter writer = mapper.writer();
        String metaJson = writer.writeValueAsString(resource);
        FileUtils.writeStringToFile(metadataFile, metaJson);
    }

    protected StorageResource readMetaDataFile(File metadataFile) throws IOException {
        if (!metadataFile.exists()) {
            throw new IOException("error metadata document not exists HTTP: 404 Conflict");
        }
        String metaJson = FileUtils.readFileToString(metadataFile);
        // configure
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        // add deserializer
        SimpleModule module = new SimpleModule("FileStorageResourceVersionDeserializer", new Version(1, 0, 0, null));
        FileStorageResourceVersionDeserializer fileStorageResourceVersionDeserializer = new FileStorageResourceVersionDeserializer();
        FileStorageResourceDeserializer fileStorageResourceDeserializer = new FileStorageResourceDeserializer();
        module.addDeserializer(StorageResourceVersion.class, fileStorageResourceVersionDeserializer);
        module.addDeserializer(StorageResource.class, fileStorageResourceDeserializer);

        mapper.registerModule(module);
        StorageResource resource = mapper.readValue(metaJson, FileStorageResource.class);
        return resource;
    }
}

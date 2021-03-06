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
package de.yaio.services.dms.server.storage.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.yaio.commons.io.IOExceptionWithCause;
import de.yaio.services.dms.api.model.*;
import de.yaio.services.dms.server.storage.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/** 
 * services for document-storage
 *  
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 */
@Service
public class FileStorageProvider implements StorageProvider {
    
    private static String VERSION_ID_FS1 = "FS1_";

    // Jackson
    @Autowired
    protected ObjectMapper mapper;

    // StorageFactory
    protected StorageFactory storageUtils;
    
    public FileStorageProvider() {
        storageUtils = StorageFactory.createStorageFactory();
    }
    
    @Value("${yaio-dms-service.storagebasedir}")
    protected String storageBaseDir;
    
    protected final DateFormat UIDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    @Override
    public StorageResource add(String appId, String srcId, String origName, InputStream uploadFile)
            throws IOExceptionWithCause, IOException {
        // create id
        String dmsId = convertSrcIdToDMSId_FS1(srcId);

        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (dirStructure.exists()) {
            throw new IOExceptionWithCause("conflict - metadataFile for resource already exists", dirStructure.getPath(),
                    new FileAlreadyExistsException(dirStructure.getPath()));
        }
        dirStructure.mkdirs();


        // resName
        String resName = convertToResourceFileName(origName, 1);

        // create resource
        StorageResourceVersion resourceVersion = new StorageResourceVersionImpl(1, origName, resName, new Date());
        StorageResource resource = new StorageResourceImpl(dmsId, srcId, 1, new Date(), new Date(), null);
        resource.getVersions().put(resourceVersion.getVersion(), resourceVersion);

        // save Uploadfile
        saveResourceFile(uploadFile, dirStructure, resName);

        // save metadata
        saveMetaDataFile(resource, getMetaDataFile(dirStructure), true);

        return resource;
    }

    @Override
    public StorageResource update(String appId, String dmsId, String origName, InputStream uploadFile)
            throws IOExceptionWithCause, IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not exists", dirStructure.getPath(),
                    new FileNotFoundException(dirStructure.getPath()));
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));

        // resName
        resource.setCurVersion(resource.getCurVersion() + 1);
        resource.setLastChanged(new Date());

        String resName = convertToResourceFileName(origName, resource.getCurVersion());
        StorageResourceVersion resourceVersion =
                new StorageResourceVersionImpl(resource.getCurVersion(), origName, resName, new Date());
        resource.getVersions().put(resourceVersion.getVersion(), resourceVersion);

        // save Uploadfile
        saveResourceFile(uploadFile, dirStructure, resName);

        // save metadata
        saveMetaDataFile(resource, getMetaDataFile(dirStructure), false);

        return resource;
    }

    @Override
    public StorageResource resetToVersion(String appId, String dmsId, Integer targetVersion)
            throws IOExceptionWithCause, IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not exists", dirStructure.getPath(),
                    new FileNotFoundException(dirStructure.getPath()));
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));

        // check targetVersion
        StorageResourceVersion resourceVersion = resource.getVersion(targetVersion);
        if (resourceVersion == null) {
            throw new IOExceptionWithCause("not found - targetVersion not exists", dirStructure.getPath(),
                    new FileNotFoundException("appId:"+ appId + " dmsId:" + dmsId + " V:" + targetVersion));
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

    @Override
    public void delete(String appId, String dmsId) throws IOExceptionWithCause, IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not exists", dirStructure.getPath(),
                    new FileNotFoundException(dirStructure.getPath()));
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

    @Override
    public StorageResource getMetaData(String appId, String dmsId) throws IOExceptionWithCause, IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not exists", dirStructure.getPath(),
                    new FileNotFoundException(dirStructure.getPath()));
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));
        return resource;
    }

    @Override
    public StorageResourceVersion getMetaData(String appId, String dmsId, Integer requestedVersion)
            throws IOExceptionWithCause, IOException {
        // check dirStructure
        File dirStructure = getDirStructure(appId, dmsId);
        if (!dirStructure.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not exists", dirStructure.getPath(),
                    new FileNotFoundException(dirStructure.getPath()));
        }

        // read metadata
        StorageResource resource = readMetaDataFile(getMetaDataFile(dirStructure));
        if (requestedVersion == null || requestedVersion.intValue() == 0) {
            requestedVersion = resource.getCurVersion();
        }
        StorageResourceVersion resourceVersion = resource.getVersion(requestedVersion);
        if (resourceVersion == null) {
            throw new IOExceptionWithCause("not found - resourceVersion not exists", dirStructure.getPath(),
                    new FileNotFoundException("appId:"+ appId + " dmsId:" + dmsId + " V:" + requestedVersion));
        }

        // return resource
        return resourceVersion;
    }

    @Override
    public Path getResource(String appId, String dmsId, Integer requestedVersion)
            throws IOExceptionWithCause, IOException {
        return getResourceFile(appId, dmsId, requestedVersion).toPath();
    }

    public File getResourceFile(String appId, String dmsId, Integer requestedVersion)
            throws IOExceptionWithCause, IOException {
        File dirStructure = getDirStructure(appId, dmsId);
        StorageResourceVersion resourceVersion = getMetaData(appId, dmsId, requestedVersion);
        return getResourceFile(dirStructure, resourceVersion.getResName());
    }

    protected String convertDMSIdToDirStructure(String appId, String dmsId) throws IOExceptionWithCause {
        if (dmsId.startsWith(VERSION_ID_FS1)) {
            return convertDMSIdToDirStructure_FS1(appId, dmsId);
        }
        throw new IOExceptionWithCause("unknwon version of dmsId:", dmsId,
                new FileNotFoundException("unknwon version of dmsId:" + dmsId));
    }
    
    protected String convertDMSIdToDirStructure_FS1(String appId, String dmsId) throws IOExceptionWithCause {
        if (!dmsId.startsWith(VERSION_ID_FS1)) {
            throw new IOExceptionWithCause("unknwon version of dmsId:", dmsId,
                    new FileNotFoundException("unknwon version of dmsId:" + dmsId));
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
        return "v" + version + "-" + normalizeFileName(origName);
    }

    protected String convertSrcIdToDMSId_FS1(String srcId) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
//            OutputStream out = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION));
//            out.write(srcId.getBytes("UTF-8"));
//            out.close();
//        } catch (IOException e) {
//            throw new AssertionError(e);
//        }
//        return VERSION_ID_FS1 + "DT" + UIDF.format(new Date()) + Base64Utils.encodeToString(baos.toByteArray());
//        return VERSION_ID_FS1 + Base64Utils.encodeToString(normalizeFileName(srcId).getBytes());
        
        // update the current checksum with the specified array of bytes
        Checksum checksum = new CRC32();
        byte bytes[] = srcId.getBytes();
        checksum.update(bytes, 0, bytes.length);
         
        // get the current checksum value
        long checksumValue = checksum.getValue();
        return VERSION_ID_FS1 + "DT" + UIDF.format(new Date()) + "_" + checksumValue;
    }
    
    protected File getDirStructure(String appId, String dmsId) throws IOExceptionWithCause {
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


    protected void saveMetaDataFile(StorageResource resource, File metadataFile, boolean create)
            throws IOExceptionWithCause, IOException {
        if (create && metadataFile.exists()) {
            throw new IOExceptionWithCause("conflict - metadataFile for resource already exists", metadataFile.getPath(),
                    new FileAlreadyExistsException(metadataFile.getPath()));
        }
        
        String metaJson = serializeMetaDataToJson(resource);
        
        FileUtils.writeStringToFile(metadataFile, metaJson);
    }

    protected StorageResource readMetaDataFile(File metadataFile) throws IOExceptionWithCause, IOException {
        if (!metadataFile.exists()) {
            throw new IOExceptionWithCause("not found - metadataFile for resource not found", metadataFile.getPath(),
                    new FileNotFoundException(metadataFile.getPath()));
        }

        String metaJson = FileUtils.readFileToString(metadataFile);
        
        return storageUtils.parseStorageResourceFromJson(metaJson);
    }

    /**
     * normalize the filename
     * @param fileName               fileName to be normalized
     * @return                       returns the fileName
     */
    protected String normalizeFileName(String fileName) {
        String newFileName = fileName;
        // replace all . but the last as extension
        newFileName = newFileName.replaceAll("\\.(?=.*\\.)", "_");

        // replace all not matching characters
        newFileName = newFileName.replaceAll("[^a-zA-Z0-9-.]", "_");

        // split by 50
        if (newFileName.length() > 90) {
            newFileName = newFileName.substring(0,  43) + "___" + newFileName.substring(newFileName.length() - 43);
        }
        return newFileName;
    }

    /**
     * serialize the resource as json
     * @param resource               resource to be serialized as json
     * @return                       returns the json
     * @throws IOException           if serialization fails
     */
    protected String serializeMetaDataToJson(StorageResource resource) throws IOException {
        ObjectWriter writer = mapper.writer();
        String metaJson = writer.writeValueAsString(resource);
        return metaJson;
    }
}

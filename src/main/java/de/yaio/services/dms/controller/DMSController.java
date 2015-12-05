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
package de.yaio.services.dms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import de.yaio.services.dms.storage.StorageProvider;
import de.yaio.services.dms.storage.StorageResource;
import de.yaio.services.dms.storage.StorageResourceVersion;


/** 
 * controller with endpoints to manage document-storage
 *  
 * @FeatureDomain                Webservice
 * @package                      de.yaio.services.dms.controller
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     document-storage
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
@Controller
@RequestMapping("${yaio-dms-service.baseurl}")
public class DMSController {

    private static final Logger LOGGER = Logger.getLogger(DMSController.class);

    @Autowired
    protected StorageProvider storageProvider;

    protected FileTypeMap fileTypeMap = FileTypeMap.getDefaultFileTypeMap();

    /** 
     * download the resource
     * @FeatureDomain                Webservice
     * @FeatureResult                return the requested resource
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @param version                version of the resource
     * @param response               servlet-response to set header-infos
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/get/{appId}/{dmsId}/{version}", 
                    method=RequestMethod.GET)
    public void handleFileDownloadById(@PathVariable("appId") final String appId,
                                       @PathVariable(value="dmsId") final String dmsId,
                                       @PathVariable(value="version") final Integer version,
                                       final HttpServletResponse response) throws IOException {
        try {
            File file = storageProvider.getResource(appId, dmsId, version).toFile();
            String fileType = fileTypeMap.getContentType(file.getName());

            MediaType mimeType = MediaType.valueOf(fileType);
            response.setContentType(mimeType.getType());
            response.setContentLength((new Long(file.length()).intValue()));
            response.setHeader("content-Disposition", "attachment; filename=" + file.getName());

            // copy it to response's OutputStream
            IOUtils.copyLarge(new FileInputStream(file), response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
            LOGGER.warn("exception while reading appId: " + appId + " dmsId: " + dmsId + " version: " + version, e);
        }
    }

    /** 
     * download the metadata of the requested resource version
     * @FeatureDomain                Webservice
     * @FeatureResult                returns metadata of the requested resource version
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @param version                version of the resource
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the requested resource version
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/getmetaversion/{appId}/{dmsId}/{version}", 
                    method=RequestMethod.GET)
    public @ResponseBody StorageResourceVersion handleFileMetaDataByVersion(@PathVariable("appId") final String appId,
                                                                            @PathVariable(value="dmsId") final String dmsId,
                                                                            @PathVariable(value="version") final Integer version,
                                                                            final HttpServletResponse response) throws IOException {
        StorageResourceVersion metaData = null;;
        try {
            metaData = storageProvider.getMetaData(appId, dmsId, version);
        } catch (IOException e) {
            LOGGER.warn("exception while reading appId: " + appId + " dmsId: " + dmsId + " version: " + version, e);
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
        }

        return metaData;
    }

    /** 
     * download the metadata of the requested resource
     * @FeatureDomain                Webservice
     * @FeatureResult                returns metadata of the requested resource version
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the requested resource version
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/getmeta/{appId}/{dmsId}", 
                    method=RequestMethod.GET)
    public @ResponseBody StorageResource handleFileMetaDataById(@PathVariable("appId") final String appId,
                                                                @PathVariable(value="dmsId") final String dmsId,
                                                                final HttpServletResponse response) throws IOException {
        StorageResource metaData = null;;
        try {
            metaData = storageProvider.getMetaData(appId, dmsId);
        } catch (IOException e) {
            LOGGER.warn("exception while reading appId: " + appId + " dmsId: " + dmsId, e);
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
        }

        return metaData;
    }

    /** 
     * add a resource to the store
     * @FeatureDomain                Webservice
     * @FeatureResult                save the resource
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param srcId                  srcId from client (based on that value the dmsId will be generated)
     * @param origFileName           original filename 
     * @param uploadFile             multipart with the upload-content
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the new resource
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/add", 
                    method=RequestMethod.POST)
    public @ResponseBody StorageResource handleFileUpload(@RequestParam("appId") final String appId,
                                                          @RequestParam("srcId") final String srcId, 
                                                          @RequestParam("origFileName") final String origFileName,
                                                          @RequestParam("file") final MultipartFile uploadFile,
                                                          final HttpServletResponse response) throws IOException{
        if (uploadFile.isEmpty()) {
            response.setStatus(400);
            response.getWriter().append("error while adding: uploadfile empty");
            return null;
        }

        try {
            StorageResource resource =  storageProvider.add(appId, srcId, origFileName, uploadFile.getInputStream());
            return resource;
        } catch (IOException e) {
            response.setStatus(409);
            response.getWriter().append("error while adding:" + e.getMessage());
            LOGGER.warn("exception while adding appId: " + appId + " srcId: " + srcId + " origFileName: " + origFileName, e);
            return null;
        }
    }

    /** 
     * updating a resource in the store
     * @FeatureDomain                Webservice
     * @FeatureResult                save the resource
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param dmsId                  dmsId of the resource
     * @param origFileName           original filename 
     * @param uploadFile             multipart with the upload-content
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the new resource
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/update", 
                    method=RequestMethod.POST)
    public @ResponseBody StorageResource handleFileUpdate(@RequestParam("appId") final String appId,
                                                          @RequestParam("dmsId") final String dmsId, 
                                                          @RequestParam("origFileName") final String origFileName,
                                                          @RequestParam("file") final MultipartFile uploadFile,
                                                          final HttpServletResponse response) throws IOException{
        if (uploadFile.isEmpty()) {
            response.setStatus(400);
            response.getWriter().append("error while updating: uploadfile empty");
            return null;
        }

        try {
            StorageResource resource = storageProvider.update(appId, dmsId, origFileName, uploadFile.getInputStream());
            return resource;
        } catch (IOException e) {
            response.setStatus(404);
            response.getWriter().append("error while updating:" + e.getMessage());
            LOGGER.warn("exception while updating appId: " + appId + " dmsId: " + dmsId + " origFileName: " + origFileName, e);
            return null;
        }
    }
}
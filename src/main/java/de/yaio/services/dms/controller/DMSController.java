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

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import de.yaio.services.dms.storage.StorageProvider;
import de.yaio.services.dms.storage.StorageResource;


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

    @Autowired
    protected StorageProvider storageProvider;
    protected MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    /** 
     * download the resource
     * @FeatureDomain                Webservice
     * @FeatureResult                return the requested resource
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param id                     id of the resource
     * @param version                version of the resource
     * @param response               servlet-response to set header-infos
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/get/{appId}/{id}/{version}", 
                    method=RequestMethod.GET)
    public void handleFileDownloadById(@PathVariable("appId") String appId,
                                       @PathVariable(value="id") String id,
                                       @PathVariable(value="version") Integer version,
                                       HttpServletResponse response) throws IOException {
        try {
            File file = storageProvider.getResource(appId, id, version).toFile();
            String mimeType = mimeTypesMap.getContentType(file);
            response.setContentType(mimeType);
            response.setContentLength((new Long(file.length()).intValue()));
            response.setHeader("content-Disposition", "attachment; filename=" + file.getName());// "attachment;filename=test.xls"
            // copy it to response's OutputStream
            IOUtils.copyLarge(new FileInputStream(file), response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /** 
     * add a resource to the store
     * @FeatureDomain                Webservice
     * @FeatureResult                save the resource
     * @FeatureKeywords              Webservice
     * @param appId                  appId of the store
     * @param id                     id of the resource
     * @param origFileName           original filename 
     * @param uploadFile             multipart with the upload-content
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the new resource
     * @throws IOException           if resource not exists
     */
    @RequestMapping(value="/add", 
                    method=RequestMethod.POST)
    public @ResponseBody StorageResource handleFileUpload(@RequestParam("appId") String appId,
                                                          @RequestParam("id") String id, 
                                                          @RequestParam("origFileName") String origFileName,
                                                          @RequestParam("file") MultipartFile uploadFile,
                                                          HttpServletResponse response) throws IOException{
        if (!uploadFile.isEmpty()) {
            try {
                StorageResource resource =  storageProvider.add(appId, id, origFileName, uploadFile.getInputStream());
                return resource;
            } catch (IOException e) {
                response.setStatus(409);
                response.getWriter().append("error while adding:" + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            response.setStatus(400);
            response.getWriter().append("error while adding: uploadfile empty");
            return null;
        }
    }
}
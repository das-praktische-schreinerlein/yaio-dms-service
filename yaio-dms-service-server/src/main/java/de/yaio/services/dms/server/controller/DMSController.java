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
package de.yaio.services.dms.server.controller;

import de.yaio.commons.io.IOExceptionWithCause;
import de.yaio.services.dms.api.model.StorageResource;
import de.yaio.services.dms.api.model.StorageResourceVersion;
import de.yaio.services.dms.server.storage.StorageProvider;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;


/** 
 * controller with endpoints to manage document-storage
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
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @param version                version of the resource
     * @param response               servlet-response to set header-infos
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    @RequestMapping(value="/get/{appId}/{dmsId}/{version}", 
                    method=RequestMethod.GET)
    public void handleFileDownloadById(@PathVariable("appId") final String appId,
                                       @PathVariable(value="dmsId") final String dmsId,
                                       @PathVariable(value="version") final Integer version,
                                       final HttpServletResponse response)
            throws IOExceptionWithCause, IOException {
        File file = storageProvider.getResource(appId, dmsId, version).toFile();
        String fileType = fileTypeMap.getContentType(file.getName());

        MediaType mimeType = MediaType.valueOf(fileType);
        response.setContentType(mimeType.getType());
        response.setContentLength((new Long(file.length()).intValue()));
        response.setHeader("content-Disposition", "attachment; filename=" + file.getName());

        // copy it to response's OutputStream
        IOUtils.copyLarge(new FileInputStream(file), response.getOutputStream());
    }

    /** 
     * download the metadata of the requested resource version
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @param version                version of the resource
     * @return                       metadata of the requested resource version
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    @RequestMapping(value="/getmetaversion/{appId}/{dmsId}/{version}", 
                    method=RequestMethod.GET)
    public @ResponseBody
    StorageResourceVersion handleFileMetaDataByVersion(@PathVariable("appId") final String appId,
                                                       @PathVariable(value="dmsId") final String dmsId,
                                                       @PathVariable(value="version") final Integer version)
            throws IOExceptionWithCause, IOException {
        return storageProvider.getMetaData(appId, dmsId, version);
    }

    /** 
     * download the metadata of the requested resource
     * @param appId                  appId of the store
     * @param dmsId                  id of the resource
     * @return                       metadata of the requested resource version
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    @RequestMapping(value="/getmeta/{appId}/{dmsId}", 
                    method=RequestMethod.GET)
    public @ResponseBody StorageResource handleFileMetaDataById(@PathVariable("appId") final String appId,
                                                                @PathVariable(value="dmsId") final String dmsId)
            throws IOExceptionWithCause, IOException {
        return storageProvider.getMetaData(appId, dmsId);
    }

    /** 
     * add a resource to the store
     * @param appId                  appId of the store
     * @param srcId                  srcId from client (based on that value the dmsId will be generated)
     * @param origFileName           original filename 
     * @param uploadFile             multipart with the upload-content
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the new resource
     * @throws IOExceptionWithCause  already exists
     * @throws IOException           errors while reading
     */
    @RequestMapping(value="/add", 
                    method=RequestMethod.POST)
    public @ResponseBody StorageResource handleFileUpload(@RequestParam("appId") final String appId,
                                                          @RequestParam("srcId") final String srcId, 
                                                          @RequestParam("origFileName") final String origFileName,
                                                          @RequestParam("file") final MultipartFile uploadFile,
                                                          final HttpServletResponse response)
            throws IOExceptionWithCause, IOException{
        if (uploadFile.isEmpty()) {
            response.setStatus(400);
            response.getWriter().append("error while adding: uploadfile empty");
            return null;
        }

        return storageProvider.add(appId, srcId, origFileName, uploadFile.getInputStream());
    }

    /** 
     * updating a resource in the store
     * @param appId                  appId of the store
     * @param dmsId                  dmsId of the resource
     * @param origFileName           original filename 
     * @param uploadFile             multipart with the upload-content
     * @param response               servlet-response to set header-infos
     * @return                       metadata of the new resource
     * @throws IOExceptionWithCause  not exists
     * @throws IOException           errors while reading
     */
    @RequestMapping(value="/update", 
                    method=RequestMethod.POST)
    public @ResponseBody StorageResource handleFileUpdate(@RequestParam("appId") final String appId,
                                                          @RequestParam("dmsId") final String dmsId, 
                                                          @RequestParam("origFileName") final String origFileName,
                                                          @RequestParam("file") final MultipartFile uploadFile,
                                                          final HttpServletResponse response)
            throws IOExceptionWithCause, IOException{
        if (uploadFile.isEmpty()) {
            response.setStatus(400);
            response.getWriter().append("error while updating: uploadfile empty");
            return null;
        }

        return storageProvider.update(appId, dmsId, origFileName, uploadFile.getInputStream());
    }

    @ExceptionHandler(IOExceptionWithCause.class)
    public void handleCustomException(final HttpServletRequest request, final IOExceptionWithCause e,
                                      final HttpServletResponse response) throws IOException {
        LOGGER.info("Exception while running request:" + createRequestLogMessage(request), e);
        if (FileAlreadyExistsException.class.isInstance(e.getCause())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().append("resource already exists");
        } else if (FileNotFoundException.class.isInstance(e.getCause())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().append("resource not found");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().append("error while serving request");
        }
    }

    @ExceptionHandler(value = {Exception.class, RuntimeException.class, IOException.class})
    public void handleAllException(final HttpServletRequest request, final Exception e,
                                   final HttpServletResponse response) {
        LOGGER.info("Exception while running request:" + createRequestLogMessage(request), e);
        response.setStatus(SC_INTERNAL_SERVER_ERROR);
        try {
            response.getWriter().append("exception while running dms for requested resource");
        } catch (IOException ex) {
            LOGGER.warn("exception while exceptionhandling", ex);
        }
    }

    protected String createRequestLogMessage(HttpServletRequest request) {
        return new StringBuilder("REST Request - ")
                .append("[HTTP METHOD:")
                .append(request.getMethod())
                .append("] [URL:")
                .append(request.getRequestURL())
                .append("] [REQUEST PARAMETERS:")
                .append(getRequestMap(request))
                .append("] [REMOTE ADDRESS:")
                .append(request.getRemoteAddr())
                .append("]").toString();
    }

    private Map<String, String> getRequestMap(HttpServletRequest request) {
        Map<String, String> typesafeRequestMap = new HashMap<>();
        Enumeration<?> requestParamNames = request.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            String requestParamName = (String)requestParamNames.nextElement();
            String requestParamValue = request.getParameter(requestParamName);
            typesafeRequestMap.put(requestParamName, requestParamValue);
        }
        return typesafeRequestMap;
    }
}
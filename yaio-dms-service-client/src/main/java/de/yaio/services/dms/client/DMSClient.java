/** 
 * software for projectmanagement and documentation
 * 
 * @FeatureDomain                Collaboration 
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     collaboration
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.services.dms.client;

import de.yaio.commons.http.HttpUtils;
import de.yaio.commons.io.IOExceptionWithCause;
import de.yaio.services.dms.api.model.StorageFactory;
import de.yaio.services.dms.api.model.StorageResource;
import de.yaio.services.dms.api.model.StorageResourceVersion;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/** 
 * dms-client
 * 
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 */
public class DMSClient {
    protected String dmsappId;
    protected String dmsurl;
    protected String dmsusername;
    protected String dmspassword;
    protected StorageFactory storageUtils;
    private int BUFFER_SIZE;

    protected DMSClient(final String dmsappId, final String dmsurl, final String dmsusername,
                        final String dmspassword, final int bufferSize) {
        this.dmsappId = dmsappId;
        this.dmsurl = dmsurl;
        this.dmsusername = dmsusername;
        this.dmspassword = dmspassword;
        this.BUFFER_SIZE = bufferSize;
        storageUtils = StorageFactory.createStorageFactory();
    }

    public static DMSClient createClient(final String dmsappId, final String dmsurl, final String dmsusername,
                                         final String dmspassword, final int bufferSize) {
        if (StringUtils.isEmpty(dmsappId)) {
            throw new IllegalArgumentException("cant create dmsclient: dmsappId must not be empty");
        }
        if (StringUtils.isEmpty(dmsurl)) {
            throw new IllegalArgumentException("cant create dmsclient: dmsurl must not be empty");
        }
        if (StringUtils.isEmpty(dmsusername)) {
            throw new IllegalArgumentException("cant create dmsclient: dmsusername must not be empty");
        }
        if (StringUtils.isEmpty(dmspassword)) {
            throw new IllegalArgumentException("cant create dmsclient: dmspassword must not be empty");
        }
        return new DMSClient(dmsappId, dmsurl, dmsusername, dmspassword, bufferSize);
    }

    public StorageResource addContentToDMS(final String srcId, final String origFileName,
                                           final InputStream input) throws IOExceptionWithCause, IOException {
        return saveResContentInDMS(true, srcId, origFileName, input);
    }

    public StorageResource updateContentInDMS(final String dmsId, final String origFileName,
                                              final InputStream input) throws IOExceptionWithCause, IOException {
        return saveResContentInDMS(false, dmsId, origFileName, input);
    }


    public InputStream getContentFromDMS(final String dmsId, final Integer version)
            throws IOExceptionWithCause, IOException {
        File tmpFile = this.getContentFileFromDMS(dmsId, version, false);
        return new FileInputStream(tmpFile);
    }

    public File getContentFileFromDMS(final String dmsId, final Integer version,
                                      final boolean useOriginalExtension) throws IOExceptionWithCause, IOException {
        String ex = ".tmp";
        if (useOriginalExtension) {
            // extract extension from dms
            StorageResourceVersion storageResVersion = this.getMetaDataForContentFromDMS(dmsId, version);
            ex = "." + FilenameUtils.getExtension(storageResVersion.getResName());
        }

        // tmp-File
        File tmpFile = File.createTempFile("download", ex);
        tmpFile.deleteOnExit();

        // call url
        String baseUrl = dmsurl + "/get/" + dmsappId + "/" + dmsId + "/" + version;
        HttpResponse response = HttpUtils.callGetUrlPure(baseUrl, dmsusername, dmspassword, null);
        HttpEntity entity = response.getEntity();

        // check response
        int retCode = response.getStatusLine().getStatusCode();
        if (retCode < 200 || retCode > 299) {
            throw new IOExceptionWithCause("error while calling dmsGetContent for dmsId", dmsId,
                    new IOException("illegal reponse:" + response.getStatusLine()
                            + " for baseurl:" + baseUrl + " with dmsId:" + dmsId
                            + " response:" + EntityUtils.toString(entity)));
        }

        // write bytes read from the input stream into the output stream
        IOUtils.write(EntityUtils.toByteArray(entity), new FileOutputStream(tmpFile));

        return tmpFile;
    }

    public StorageResourceVersion getMetaDataForContentFromDMS(final String dmsId, final Integer version)
            throws IOExceptionWithCause, IOException {
        // call url
        String baseUrl = dmsurl + "/getmetaversion/" + dmsappId + "/" + dmsId + "/" + version;
        HttpEntity entity;
        HttpResponse response;
        response = HttpUtils.callGetUrlPure(baseUrl, dmsusername, dmspassword, null);
        entity = response.getEntity();

        // check response
        int retCode = response.getStatusLine().getStatusCode();
        if (retCode < 200 || retCode > 299) {
            throw new IOExceptionWithCause("error while calling dmsGetMetaData for dmsId", dmsId,
                    new IOException("illegal reponse:" + response.getStatusLine()
                            + " for baseurl:" + baseUrl + " with dmsId:" + dmsId
                            + " response:" + EntityUtils.toString(entity)));
        }

        // configure
        String metaJson = EntityUtils.toString(entity);
        return storageUtils.parseStorageResourceVersionFromJson(metaJson);
    }


    protected StorageResource saveResContentInDMS(final boolean flgNew, final String id, final String origFileName,
                                                  final InputStream input) throws IOExceptionWithCause,IOException  {
        // upload file
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", dmsappId);

        // use different id-params
        if (flgNew) {
            params.put("srcId", id);
        } else {
            params.put("dmsId", id);
        }
        params.put("origFileName", origFileName);
        Map<String, String> binfileParams = new HashMap<String, String>();
        File tmpFile = File.createTempFile("upload", "tmp");
        binfileParams.put("file", tmpFile.getCanonicalPath());
        tmpFile.deleteOnExit();

        // write bytes read from the input stream into the output stream
        OutputStream outStream = new FileOutputStream(tmpFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = input.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        input.close();
        outStream.close();

        // call url
        String baseUrl = dmsurl;
        HttpResponse response;
        if (flgNew) {
            baseUrl += "/add";
            response = HttpUtils.callPostUrlPure(baseUrl, dmsusername, dmspassword,
                    params, null, binfileParams);
        } else {
            baseUrl += "/update";
            response = HttpUtils.callPostUrlPure(baseUrl, dmsusername, dmspassword,
                    params, null, binfileParams);
        }
        HttpEntity entity = response.getEntity();

        // check response
        int retCode = response.getStatusLine().getStatusCode();
        if (retCode < 200 || retCode > 299) {
            throw new IOExceptionWithCause("error while calling dmsGetContent for id", id,
                    new IOException("illegal reponse:" + response.getStatusLine()
                            + " for baseurl:" + baseUrl + " with id:" + id
                            + " response:" + EntityUtils.toString(entity)));
        }

        // configure
        String metaJson = EntityUtils.toString(entity);
        return storageUtils.parseStorageResourceFromJson(metaJson);
    }
}

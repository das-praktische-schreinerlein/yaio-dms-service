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
package de.yaio.services.dms.server.configuration;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/** 
 * the dms-service as spring boot application
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("de.yaio.services.dms")
public class DMSServerConfig {

    @Bean
    MultipartConfigElement configureMultipartConfigElement() {
        // spring-config
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(System.getProperty("yaio.server.maxfilesize", "128kb"));
        factory.setMaxRequestSize(System.getProperty("yaio.server.maxrequestsize", "128kb"));
        MultipartConfigElement config = factory.createMultipartConfig();
        
        // tomcat-config
        FileUploadBase tomcatConfig = new FileUpload();
        tomcatConfig.setFileSizeMax(config.getMaxFileSize());
        tomcatConfig.setSizeMax(config.getMaxFileSize());

        return config;
    }
}
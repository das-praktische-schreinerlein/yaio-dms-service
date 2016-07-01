Yaio - DMS-Service
=====================

# Desc
A webservice with document-management-functionality.


# Build and run
- test it

        mvn install
        java -Xmx768m -Xms128m -Dspring.config.location=file:config/dms-application.properties -Dlog4j.configuration=file:config/log4j.properties -cp "dist/yaio-dms-service-full.jar" de.yaio.services.dms.server.DMSApplication --config config/dms-application.properties
        curl --user admin:secret -X POST http://localhost:8083/services/dms/add -F 'appId=yaio-playground' -F 'srcId=8767868768768768769' -F 'origFileName=testfile.xxx' -F 'file=@/cygdrive/d/tmp/readme.txt'
        // get dmsId from response
        curl --user admin:secret -X POST http://localhost:8083/services/dms/update -F 'appId=yaio-playground' -F 'dmsId=${dmsId}' -F 'origFileName=testfile.xxx' -F 'file=@/cygdrive/d/tmp/readme.txt'
        curl --user admin:secret -X GET http://localhost:8083/services/dms/getmeta/yaio-playground/${dmsId}
        curl --user admin:secret -X GET http://localhost:8083/services/dms/getmetaversion/yaio-playground/${dmsId}/0
        curl --user admin:secret -X GET http://localhost:8083/services/dms/get/yaio-playground/${dmsId}/0

# Thanks to
- **Build-Tools**
    - [Apache Maven](https://github.com/apache/maven)
    - [Eclipse](http://eclipse.org/)
- **Java-Core-Frameworks**
    - [Spring-Framework](https://github.com/spring-projects/spring-framework)
    - [Spring-boot](https://github.com/spring-projects/spring-boot)
    - [Spring Security](https://github.com/spring-projects/spring-security)

# License
    /**
     * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
     * @category collaboration
     * @copyright Copyright (c) 2010-2014, Michael Schreiner
     * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
     *
     * This Source Code Form is subject to the terms of the Mozilla Public
     * License, v. 2.0. If a copy of the MPL was not distributed with this
     * file, You can obtain one at http://mozilla.org/MPL/2.0/.
     */

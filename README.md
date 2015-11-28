Yaio - DMS-Service
=====================

# Desc
A webservice with document-management-functionality.


# Build and run
- test it

        mvn install
        mvn spring-boot:run
        curl --user admin:secret -X POST http://localhost:8083/services/dms/add -F 'appId=yaio-playground' -F 'id=8767868768768768769' -F 'origFileName=testfile.xxx' -F 'file=@/cygdrive/d/tmp/readme.txt'
        curl --user admin:secret -X POST http://localhost:8083/services/dms/update -F 'appId=yaio-playground' -F 'id=8767868768768768769' -F 'origFileName=testfile.xxx' -F 'file=@/cygdrive/d/tmp/readme.txt'
        curl --user admin:secret -X GET http://localhost:8083/services/dms/getmeta/yaio-playground/8767868768768768769
        curl --user admin:secret -X GET http://localhost:8083/services/dms/getmetaversion/yaio-playground/8767868768768768769/0
        curl --user admin:secret -X GET http://localhost:8083/services/dms/get/yaio-playground/8767868768768768769/0

- to build it as standalone-jar with all dependencies take a look at pom.xml

        <!-- packaging - change it with "mvn package -Dpackaging.type=jar" -->
        <packaging.type>jar</packaging.type>
        <!-- assembly a jar with all dependencies - activate it with "mvn package -Dpackaging.assembly-phase=package" -->
        <packaging.assembly-phase>none</packaging.assembly-phase>
        <!-- shade to an ueber-jar - activate it with "mvn package -Dpackaging.shade-phase=package" -->
        <packaging.shade-phase>none</packaging.shade-phase>
        <!-- prepare for springboot - activate it with "mvn package -Dpackaging.springboot-phase=package" -->
        <packaging.springboot-phase>none</packaging.springboot-phase>

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

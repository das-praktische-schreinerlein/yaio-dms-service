Yaio - DMS-Service
=====================

# Desc
A webservice with document-management-functionality.


# Build and run
- test it

        mvn install
        mvn spring-boot:run
        curl --user dms:secret -X POST http://localhost:8083/services/dms/add -F 'appId=yaio-playground' -F 'id=876786876876876876' -F 'origFileName=testfile.xxx' -F 'file=@/cygdrive/d/tmp/readme.txt'
        curl --user dms:secret -X GET http://localhost:8083/services/dms/get/yaio-playground/876786876876876876/1 

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

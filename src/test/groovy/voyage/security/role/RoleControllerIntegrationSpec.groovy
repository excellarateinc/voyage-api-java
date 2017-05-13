/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package voyage.security.role

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest

class RoleControllerIntegrationSpec extends AuthenticatedIntegrationTest {
    private static final Long ROLE_STANDARD_ID = 2

    @Autowired
    private RoleService roleService

    def '/api/v1/roles GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/roles GET - Super User access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 2
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'Super User'
            responseEntity.body[0].authority == 'role.super'
    }

    def '/api/v1/roles GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/roles GET - Standard User with permission "api.roles.list" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.list')

        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 2
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'Super User'
            responseEntity.body[0].authority == 'role.super'
    }

    def '/api/v1/roles POST - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/roles', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/roles POST - Super User access granted'() {
        given:
            Role role = new Role(name:'Role-Name-1', authority:'test.role.authority.1')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Role> responseEntity = POST('/api/v1/roles', httpEntity, Role, superClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/api/v1/roles/3'
            responseEntity.body.id
            responseEntity.body.name == 'Role-Name-1'
            responseEntity.body.authority == 'test.role.authority.1'
    }

    def '/api/v1/roles POST - Standard User access denied'() {
        given:
            Role role = new Role(name:'Role-Name-2', authority:'test.role.authority.2')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/roles', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/roles POST - Standard User with permission "api.roles.create" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.create')

            Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Role> responseEntity = POST('/api/v1/roles', httpEntity, Role, standardClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/api/v1/roles/4'
            responseEntity.body.id
            responseEntity.body.name == 'Role-Name-3'
            responseEntity.body.authority == 'test.role.authority.3'
    }

    def '/api/v1/roles/{id} GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/roles/{id} GET - Super User access granted'() {
        when:
            ResponseEntity<Role> responseEntity = GET('/api/v1/roles/1', Role, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'Super User'
            responseEntity.body.authority == 'role.super'
    }

    def '/api/v1/roles/{id} GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/roles/{id} GET - Standard User with permission "api.roles.get" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.get')

        when:
            ResponseEntity<Role> responseEntity = GET('/api/v1/roles/1', Role, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'Super User'
            responseEntity.body.authority == 'role.super'
    }

    def '/api/v1/roles/{id} GET - Invalid ID returns a 404 Not Found response'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/roles/999999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/roles/{id} PUT - Anonymous access denied'() {
        given:
            Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/roles/1', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/roles/{id} PUT - Super User access granted'() {
        given:
            Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
            role = roleService.saveDetached(role)

            role.name = 'Role-Name-3-Updated'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Role> responseEntity = PUT('/api/v1/roles/1', httpEntity, Role, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == role.id
            responseEntity.body.name == 'Role-Name-3-Updated'
            responseEntity.body.authority == 'test.role.authority.3'
    }

    def '/api/v1/roles/{id} PUT - Standard User access denied'() {
        given:
            Role role = new Role(name:'Role-Name-4', authority:'test.role.authority.4')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/roles/1', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/roles/{id} PUT - Standard User with permission "api.roles.update" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.update')

            Role role = new Role(name:'Role-Name-5', authority:'test.role.authority.5')
            role = roleService.saveDetached(role)

            role.name = 'Role-Name-5-Updated'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
            ResponseEntity<Role> responseEntity = PUT('/api/v1/roles/1', httpEntity, Role, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == role.id
            responseEntity.body.name == 'Role-Name-5-Updated'
            responseEntity.body.authority == 'test.role.authority.5'
    }

    def '/api/v1/roles/{id} PUT - Invalid ID returns a 404 Not Foundresponse'() {
        given:
            Role role = new Role(id:9999, name:'Role-Name-5', authority:'test.role.authority.5')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
           ResponseEntity<Iterable> responseEntity = PUT('/api/v1/roles/999999', httpEntity, Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/roles/{id} DELETE - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/roles/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/roles/{id} DELETE - Super User access granted'() {
        given:
            Role role = new Role(name:'Role-Name-5', authority:'test.role.authority.5')
            role = roleService.saveDetached(role)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/roles/${role.id}", String, superClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/roles/{id} DELETE - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/roles/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/roles/{id} DELETE - Standard User with permission "api.roless.delete" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.delete')

            Role role = new Role(name:'Role-Name-6', authority:'test.role.authority.6')
            role = roleService.saveDetached(role)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/roles/${role.id}", String, standardClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/roles/{id} DELETE - Invalid ID returns a 404 Not Found response'() {
        when:
           ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/roles/999999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }
}

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

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

// TODO Needs to be rewritten to focus on inputs and outputs. Right now these tests are not validating the entire JSON response or JSON request
// TODO Remove the Exception tests because they do NOTHING! What's the point of these at all?
// TODO Add NEW exception tests for exceptions that are actually thrown by the Service classes (ImmutableRecordException,
//      UnknownIdentifierException, ValidationException...)
class RoleControllerSpec extends Specification {
    Role role
    RoleService roleService = Mock(RoleService)
    RoleController roleController = new RoleController(roleService)

    def setup() {
        role = new Role(id:1, name:'Super User', authority:'ROLE_SUPER')
    }

    def 'list - fetch data from RoleService'() {
        when:
            ResponseEntity<Role> roles = roleController.list()
        then:
            1 * roleService.listAll() >> [role]
            roles != null
            HttpStatus.OK == roles.statusCode

        when:
            roleController.list()
        then:
            1 * roleService.listAll() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'get - fetch data from RoleService'() {
        when:
            ResponseEntity<Role> role = roleController.get(1)
        then:
            1 * roleService.get(1) >> role
            role != null
            HttpStatus.OK == role.statusCode
            'Super User' == role.body.name
            'ROLE_SUPER' == role.body.authority

        when:
            roleController.get(1)
        then:
            1 * roleService.get(1) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'save - calling RoleService to save object'() {
        when:
            ResponseEntity<Role> response = roleController.save(role)
        then:
            1 * roleService.saveDetached(role) >> role
            response != null
            HttpStatus.CREATED == response.statusCode
            '/api/v1/roles/1' == response.headers.location[0]
            'Super User' == response.body.name
            'ROLE_SUPER' == response.body.authority

        when:
            roleController.save(role)
        then:
            1 * roleService.saveDetached(role) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'update - calling RoleService to save incoming object'() {
        setup:
            Role modifiedRole = new Role(id:1, name:'Super User', authority:'ROLE_ADMIN')

        when:
            ResponseEntity<Role> updatedRole = roleController.update(modifiedRole)
        then:
            1 * roleService.saveDetached(modifiedRole) >> modifiedRole
            updatedRole != null
            HttpStatus.OK == updatedRole.statusCode

        when:
            roleController.update(modifiedRole)
        then:
            1 * roleService.saveDetached(modifiedRole) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'delete - calling RoleService with the role id'() {
        when:
            ResponseEntity response = roleController.delete(1)
        then:
            1 * roleService.delete(1)
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            roleController.delete(1)
        then:
            1 * roleService.delete(1) >> { throw new Exception() }
            thrown(Exception)
    }
}

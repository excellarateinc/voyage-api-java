/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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

import spock.lang.Specification
import voyage.security.permission.PermissionService

class RoleServiceSpec extends Specification {
    Role role
    Role modifiedRole
    RoleRepository roleRepository = Mock()
    PermissionService permissionService = Mock()
    RoleService roleService = new RoleService(roleRepository, permissionService)

    def setup() {
        role = new Role(name:'Super User', authority:'ROLE_SUPER')
        modifiedRole = new Role(name:'Super Admin', authority:'ROLE_SUPER')
    }

    def 'listAll - returns a single result' () {
        setup:
            roleRepository.findAllByIsDeletedFalse() >> [role]
        when:
            Iterable<Role> roleList = roleService.listAll()
        then:
            1 == roleList.size()
    }

    def 'save - applies the values and calls the roleRepository' () {
        setup:
            roleRepository.save(_ as Role) >> role
        when:
            Role savedRole = roleService.saveDetached(role)
        then:
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
            !savedRole.isDeleted
    }

    def 'get - calls the roleRepository.findOne' () {
        setup:
            roleRepository.findByIdAndIsDeletedFalse(_ as Long) >> role
        when:
            Role fetchedRole = roleService.get(1L)
        then:
            'Super User' == fetchedRole.name
            'ROLE_SUPER' == fetchedRole.authority
            !fetchedRole.isDeleted
    }

    def 'delete - verifies the object and calls roleRepository.delete' () {
        setup:
            roleRepository.findByIdAndIsDeletedFalse(_ as Long) >> role
        when:
            roleService.delete(1L)
        then:
            role.isDeleted
    }

    def 'addPermission - inserts the permission if it does not already exist'() {
        setup:
            roleRepository.save(_ as Role) >> role
        when:
            Role savedRole = roleService.saveDetached(role)
        then:
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
            !savedRole.isDeleted
    }

    def 'findByAuthority - passes the request to the roleRepository'() {
        setup:
            roleRepository.findByAuthorityAndIsDeletedFalse('anything') >> role
        when:
            Role savedRole = roleService.findByAuthority('anything')
        then:
            savedRole == role
    }
}

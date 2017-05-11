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

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.common.error.UnknownIdentifierException
import voyage.security.permission.Permission
import voyage.security.permission.PermissionService

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service('roleService')
@Validated
class RoleService {
    private final RoleRepository roleRepository
    private final PermissionService permissionService

    RoleService(RoleRepository roleRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository
        this.permissionService = permissionService
    }

    void delete(@NotNull Long id) {
        Role role = get(id)
        role.isDeleted = true
        roleRepository.save(role)
    }

    Role get(@NotNull Long id) {
        Role role = roleRepository.findOne(id)
        if (!role) {
            throw new UnknownIdentifierException()
        }
        return role
    }

    Iterable<Role> listAll() {
        return roleRepository.findAll()
    }

    Role saveDetached(@Valid Role role) {
        if (role.id) {
            Role existingRole = get(role.id)
            existingRole.with {
                name = role.name
                isDeleted = role.isDeleted
            }
            return roleRepository.save(existingRole)
        }
        return roleRepository.save(role)
    }

    /**
     * Used by integration tests to quickly add a permission to validate that access is allowing/denying based on the
     * permission name.
     */
    void addPermission(@NotNull Long roleId, @NotNull String permissionName) {
        Permission permission = permissionService.findByName(permissionName)
        Role role = get(roleId)
        role.permissions.add(permission)
        saveDetached(role)
    }
}

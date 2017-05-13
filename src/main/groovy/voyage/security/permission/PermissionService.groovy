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
package voyage.security.permission

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.common.error.UnknownIdentifierException

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class PermissionService {
    private final PermissionRepository permissionRepository

    PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository
    }

    void delete(@NotNull Long id) {
        Permission permission = get(id)
        if (permission.isImmutable) {
            throw new ImmutableRecordException()
        }
        permission.isDeleted = true
        permissionRepository.save(permission)
    }

    Permission findByName(@NotNull String name) {
        Permission permission = permissionRepository.findByName(name)
        if (!permission) {
            throw new UnknownIdentifierException("Unknown permission name given: ${name}")
        }
        return permission
    }

    Iterable<Permission> findAllByUser(@NotNull Long userId) {
        permissionRepository.findAllByUserId(userId)
    }

    Iterable<Permission> findAllByClient(@NotNull Long clientId) {
        permissionRepository.findAllByClientId(clientId)
    }

    Permission get(@NotNull Long id) {
        Permission permission = permissionRepository.findOne(id)
        if (!permission) {
            throw new UnknownIdentifierException()
        }
        return permission
    }

    Iterable<Permission> listAll() {
        return permissionRepository.findAll()
    }

    Permission saveDetached(@Valid Permission permission) {
        if (permission.id) {
            Permission existingPermission = get(permission.id)
            if (existingPermission.isImmutable) {
                throw new ImmutableRecordException()
            }
            existingPermission.with {
                name = permission.name
                description = permission.description
                isDeleted = permission.isDeleted
            }
            return permissionRepository.save(existingPermission)
        }
        return permissionRepository.save(permission)
    }
}

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

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PermissionRepository extends CrudRepository<Permission, Long> {

    @Query('FROM Permission p WHERE p.id = ?1 AND p.isDeleted = false')
    Permission findOne(Long id)

    @Query('FROM Permission p WHERE p.isDeleted = false')
    Iterable<Permission> findAll()

    @Query('''SELECT permission
                FROM User as user
                JOIN user.roles as role
                JOIN role.permissions as permission
                WHERE user.id = ?1
                AND user.isDeleted = false
                AND role.isDeleted = false
                AND permission.isDeleted = false
                ORDER BY permission.name ASC''')
    Iterable<Permission> findAllByUserId(Long id)

    @Query('''SELECT permission
                FROM Client as client
                JOIN client.roles as role
                JOIN role.permissions as permission
                WHERE client.id = ?1
                AND client.isDeleted = false
                AND role.isDeleted = false
                AND permission.isDeleted = false
                ORDER BY permission.name ASC''')
    Iterable<Permission> findAllByClientId(Long id)

    @Query('FROM Permission p WHERE p.name =?1 AND p.isDeleted = false')
    Permission findByName(String name)
}

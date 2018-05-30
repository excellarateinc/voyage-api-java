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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/roles'])
class RoleController {
    private final RoleService roleService

    @Autowired
    RoleController(RoleService roleService) {
        this.roleService = roleService
    }

    /**
     * @api {get} /v1/roles Get all roles
     * @apiVersion 1.0.0
     * @apiName RoleList
     * @apiGroup Role
     *
     * @apiDescription list all the roles
     *
     * @apiPermission api.roles.list
     *
     * @apiSampleRequest http://voyage.com/api/v1/roles
     *
     * @apiUse AuthHeader
     *
     * @apiUse RoleListModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.roles.list')")
    ResponseEntity list() {
        Iterable<Role> roles = roleService.listAll()
        return new ResponseEntity(roles, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/roles Create role
     * @apiVersion 1.0.0
     * @apiName RoleCreate
     * @apiGroup Role
     *
     * @apiDescription Create a new role and add it to the existing list
     *
     * @apiPermission api.roles.create
     *
     * @apiSampleRequest http://voyage.com/api/v1/roles
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/roles/1"
     * }
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.roles.create')")
    ResponseEntity save(@RequestBody Role role) {
        Role newRole = roleService.saveDetached(role)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/api/v1/roles/${newRole.id}")
        return new ResponseEntity(newRole, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/roles/:roleId Get a role
     * @apiVersion 1.0.0
     * @apiName RoleGet
     * @apiGroup Role
     *
     * @apiDescription get the existing role based on id.
     *
     * @apiPermission api.roles.get
     *
     * @apiSampleRequest http://voyage.com/api/v1/roles/1
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} roleId Role ID
     *
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        Role roleFromDB = roleService.get(id)
        return new ResponseEntity(roleFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/roles/:roleId Delete a role
     * @apiVersion 1.0.0
     * @apiName RoleDelete
     * @apiGroup Role
     *
     * @apiDescription delete role based on id.
     *
     * @apiPermission api.roles.delete
     *
     * @apiSampleRequest http://voyage.com/api/v1/roles/1
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} roleId Role ID
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        roleService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/roles/:roleId Update a role
     * @apiVersion 1.0.0
     * @apiName RoleUpdate
     * @apiGroup Role
     *
     * @apiDescription update role based on id.
     *
     * @apiPermission api.roles.update
     *
     * @apiSampleRequest http://voyage.com/api/v1/roles
     *
     * @apiUse AuthHeader
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.update')")
    ResponseEntity update(@RequestBody Role role) {
        Role modifiedRole = roleService.saveDetached(role)
        return new ResponseEntity(modifiedRole, HttpStatus.OK)
    }
}

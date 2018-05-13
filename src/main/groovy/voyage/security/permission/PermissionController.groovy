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
package voyage.security.permission

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
@RequestMapping(['/api/v1/permissions', '/api/v1.0/permissions'])
class PermissionController {
    private final PermissionService permissionService

    @Autowired
    PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService
    }

    /**
     * @api {get} /v1/permissions Get all permissions
     * @apiVersion 1.0.0
     * @apiName PermissionList
     * @apiGroup Permission
     *
     * @apiDescription list all the permissions
     *
     * @apiPermission api.permissions.list
     *
     * @apiSampleRequest http://voyage.com/api/v1/permissions
     *
     * @apiUse AuthHeader
     *
     * @apiUse PermissionListModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.permissions.list')")
    ResponseEntity list() {
        Iterable<Permission> permissions = permissionService.listAll()
        return new ResponseEntity(permissions, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/permissions Create permission
     * @apiVersion 1.0.0
     * @apiName PermissionCreate
     * @apiGroup Permission
     *
     * @apiDescription Create a new permission and add it the existing list
     *
     * @apiPermission api.permissions.create
     *
     * @apiSampleRequest http://voyage.com/api/v1/permissions/
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/permissions/1"
     * }
     *
     * @apiUse PermissionRequestModel
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.permissions.create')")
    ResponseEntity save(@RequestBody Permission permission) {
        Permission newPermission = permissionService.saveDetached(permission)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/api/v1/permissions/${newPermission.id}")
        return new ResponseEntity(newPermission, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/permissions/:permissionId Get a permission
     * @apiVersion 1.0.0
     * @apiName PermissionGet
     * @apiGroup Permission
     *
     * @apiDescription get the existing permissions based on id.
     *
     * @apiPermission api.permissions.get
     *
     * @apiSampleRequest http://voyage.com/api/v1/permissions/1
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} permissionId Permission ID
     *
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        Permission permissionFromDB = permissionService.get(id)
        return new ResponseEntity(permissionFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/permissions/:permissionId Delete a permission
     * @apiVersion 1.0.0
     * @apiName PermissionDelete
     * @apiGroup Permission
     *
     * @apiDescription delete the existing permission
     *
     * @apiPermission api.permissions.delete
     *
     * @apiSampleRequest http://voyage.com/api/v1/permissions/id
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} permissionId Permission ID
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     * @apiUse UnknownIdentifierError
     * @apiUse ImmutableRecordError
     **/
    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        permissionService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/permissions/:permissionId Update a permission
     * @apiVersion 1.0.0
     * @apiName PermissionUpdate
     * @apiGroup Permission
     *
     * @apiDescription Update the existing permission
     *
     * @apiPermission api.permissions.update
     *
     * @apiSampleRequest http://voyage.com/api/v1/permissions/id
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the updated resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/permissions/1"
     * }
     *
     * @apiUse PermissionRequestModel
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     * @apiUse ImmutableRecordError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.update')")
    ResponseEntity update(@RequestBody Permission permission) {
        Permission modifiedPermission = permissionService.saveDetached(permission)
        return new ResponseEntity(modifiedPermission, HttpStatus.OK)
    }
}

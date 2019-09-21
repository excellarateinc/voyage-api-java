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
package voyage.security.user

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
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    private final UserService userService

    @Autowired
    UserController(UserService userService) {
        this.userService = userService
    }

    /**
     * @api {get} /v1/users Get all users
     * @apiVersion 1.0.0
     * @apiName UserList
     * @apiGroup User
     *
     * @apiDescription shows the list of users
     *
     * @apiSampleRequest http://voyage.com/api/v1/users/
     *
     * @apiPermission api.user.list
     *
     * @apiUse AuthHeader
     *
     * @apiUse UserListModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.users.list')")
    ResponseEntity list() {
        Iterable<User> users = userService.listAll()
        return new ResponseEntity(users, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/users Create user
     * @apiVersion 1.0.0
     * @apiName UserCreate
     * @apiGroup User
     *
     * @apiDescription create a new user
     *
     * @apiSampleRequest http://voyage.com/api/v1/users/
     *
     * @apiPermission api.user.create
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/users/1"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     * @apiUse TooManyPhonesError
     * @apiUse PhoneNumberInvalidError
     * @apiUse WeakPasswordError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.users.create')")
    ResponseEntity save(@RequestBody User user) {
        User newUser = userService.saveDetached(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/api/v1/users/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/users/:userId Get a user
     * @apiVersion 1.0.0
     * @apiName UserGet
     * @apiGroup User
     *
     * @apiDescription get user details based on User Id
     *
     * @apiSampleRequest http://voyage.com/api/v1/users/1
     *
     * @apiPermission api.user.get
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} userId User ID
     *
     *
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        User userFromDB = userService.get(id)
        return new ResponseEntity(userFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/users/:userId Delete a user
     * @apiVersion 1.0.0
     * @apiName UserDelete
     * @apiGroup User
     *
     * @apiDescription delete the existing user
     *
     * @apiSampleRequest http://voyage.com/api/v1/users/1
     *
     * @apiPermission api.user.delete
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} userId User ID
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
    @PreAuthorize("hasAuthority('api.users.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        userService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/users/:userId Update a user
     * @apiVersion 1.0.0
     * @apiName UserUpdate
     * @apiGroup User
     *
     * @apiDescription update the existing user details
     *
     * @apiSampleRequest http://voyage.com/api/v1/users
     *
     * @apiPermission api.user.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     * @apiUse UnknownIdentifierError
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     * @apiUse TooManyPhonesError
     * @apiUse PhoneNumberInvalidError
     * @apiUse WeakPasswordError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.update')")
    ResponseEntity update(@RequestBody User user) {
        User modifiedUser = userService.saveDetached(user)
        return new ResponseEntity(modifiedUser, HttpStatus.OK)
    }
}

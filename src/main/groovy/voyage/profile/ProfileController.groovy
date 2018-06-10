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
package voyage.profile

import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

import voyage.core.error.ErrorResponse
import voyage.security.user.MobilePhoneRequiredException
import voyage.security.user.User
import voyage.security.user.UserService
import voyage.security.user.UsernameAlreadyInUseException
import voyage.security.verify.VerifyService

@RestController
@RequestMapping(path=['/api/v1/profiles'], produces = 'application/json')
@Api(tags = 'Profile', description = 'endpoints for registering and maintaining a user profile within the voyage platform')
class ProfileController {
    private final ProfileService profileService
    private final VerifyService userVerifyService
    private final UserService userService

    @Autowired
    ProfileController(ProfileService profileService, VerifyService userVerifyService, UserService userService) {
        this.profileService = profileService
        this.userVerifyService = userVerifyService
        this.userService = userService
    }

    @PostMapping('/register')
    @ApiOperation(value = 'Creates a new user profile. All parameters are required and at least 1 mobile phone must be added.')
    @ApiResponses(value = [
            @ApiResponse(code = 201,
                    message = 'Craeted',
                    responseHeaders = @ResponseHeader(name = 'Location',
                            description = 'https://my-app/api/v1/profiles/me')),
            @ApiResponse(code = 400,
                    message = 'Bad Request', reference = 'ErrorResponse', response = ErrorResponse.class)
    ])
//    new ResponseMessageBuilder()
//    .code(400)
//    .message('400 Bad Request')
//    .responseModel(new ModelRef(ERROR_RESPONSE))
//    .build(),
//    new ResponseMessageBuilder()
//    .code(401)
//    .message('401 Unauthorized')
//    .responseModel(new ModelRef(ERROR_RESPONSE))
//    .build(),
//    new ResponseMessageBuilder()
//    .code(403)
//    .message('403 Forbidden')
//    .responseModel(new ModelRef(ERROR_RESPONSE))
//    .build(),
//    new ResponseMessageBuilder()
//    .code(404)
//    .message('404 Not Found')
//    .responseModel(new ModelRef(ERROR_RESPONSE))
//    .build(),

    ResponseEntity<Void> register(
            @RequestBody User user) throws UsernameAlreadyInUseException, MobilePhoneRequiredException {
        profileService.register(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, '/v1/profiles/me')
        return new ResponseEntity(headers, HttpStatus.CREATED)
    }

    @GetMapping('/me')
    @PreAuthorize("hasAuthority('api.profiles.me')")
    @ApiOperation(value = 'Gets the current users profile.')
    @ApiResponses(value = [
            @ApiResponse(code = 401, message = 'Unauthorized', reference = 'ErrorResponse', response = ErrorResponse.class),
            @ApiResponse(code = 403, message = 'Forbidden', reference = 'ErrorResponse', response = ErrorResponse.class)
    ])
    ResponseEntity<User> myProfile() {
        User user = userService.currentUser
        return new ResponseEntity(user, HttpStatus.OK)
    }
}

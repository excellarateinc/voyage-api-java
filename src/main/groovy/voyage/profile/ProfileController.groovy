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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import voyage.security.user.User
import voyage.security.user.UserService
import voyage.security.verify.VerifyService

@RestController
@RequestMapping(['/api/v1/profiles', '/api/v1.0/profiles'])
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

    /**
     * @api {post} /v1/profile Create profile
     * @apiVersion 1.0.0
     * @apiName ProfileCreate
     * @apiGroup Profile
     *
     * @apiDescription Creates a new user profile. All parameters are required and at least 1 mobile phone must be added.
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} New Profile Location
     * HTTP/1.1 201: Created
     * {
     *     "Location": "https://my-app/api/v1/profiles/me"
     * }
     *
     * @apiUse ProfileRequestModel
     * @apiUse ProfileSuccessModel
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     * @apiUse TooManyPhonesError
     * @apiUse PhoneNumberInvalidError
     * @apiUse MailSendError
     **/
    @PostMapping('/register')
    ResponseEntity register(@RequestBody User userIn) {
        profileService.register(userIn)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, '/v1/profiles/me')
        return new ResponseEntity(headers, HttpStatus.CREATED)
    }

    @GetMapping('/me')
    @PreAuthorize("hasAuthority('api.profiles.me')")
    ResponseEntity myProfile() {
        User user = userService.currentUser
        return new ResponseEntity(user, HttpStatus.OK)
    }
}

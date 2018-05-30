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

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import voyage.security.user.User
import voyage.security.user.UserService
import voyage.security.verify.VerifyService

@RestController
@RequestMapping(['/api/v1/profiles'])
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

    /**
     * @api { p o s t } /v1/profile Create profile
     * @apiVersion 1.0.0
     * @apiName ProfileCreate
     * @apiGroup Profile
     *
     * @apiDescription
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiParam { O b j e c t } profile Profile
     * @apiParam { S t r i n g } profile.userName Username of the user
     * @apiParam { S t r i n g } profile.email Email
     * @apiParam { S t r i n g } profile.firstName First name
     * @apiParam { S t r i n g } profile.lastName Last name
     * @apiParam { S t r i n g } profile.password Password
     * @apiParam { O b j e c t [ ] } profile.phones Profile phone numbers
     * @apiParam { S t r i n g } profile.phones.phoneNumber Phone number in E.164 format (ie +16518886021 or +1-651-888-6021 as punctuation is stripped out)
     * @apiParam { S t r i n g } profile.phones.phoneType Phone type (mobile, office, home, other). NOTE: At least one mobile phone is required.
     *
     * @apiExample { j s o n } Example body:
     *{
     *     "firstName": "FirstName",
     *     "lastName": "LastName",
     *     "username": "FirstName3@app.com",
     *     "email": "FirstName3@app.com",
     *     "password": "my-secure-password",
     *     "phones":
     *     [
     *{
     *             "phoneType": "MOBILE",
     *             "phoneNumber" : "+6518886021"
     *}*     ]
     *}*
     * @apiHeader ( Response Headers ) {String} location Location of the newly created resource
     *
     * @apiHeaderExample { j s o n } New Profile Location
     * HTTP/1.1 201: Created
     *{
     *     "Location": "https://my-app/api/v1/profile"
     *}*
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     * */
    @PostMapping('/register')
    @ApiOperation(value = 'Creates a new user profile. All parameters are required and at least 1 mobile phone must be added.')
    ResponseEntity<Void> register(@RequestBody User userIn) {
        profileService.register(userIn)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, '/v1/profiles/me')
        return new ResponseEntity(headers, HttpStatus.CREATED)
    }

    @GetMapping('/me')
    @PreAuthorize("hasAuthority('api.profiles.me')")
    @ApiOperation(value = 'Gets the current users profile.')
    ResponseEntity<User> myProfile() {
        User user = userService.getCurrentUser()
        return new ResponseEntity(user, HttpStatus.OK)
    }
}

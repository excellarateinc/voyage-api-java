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
package voyage.security.verify

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/verify', '/api/v1.0/verify'])
class VerifyController {
    private final VerifyService verifyService

    @Autowired
    VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService
    }

    /**
     * @api {post} /v1/verify/send Send Verify Code
     * @apiVersion 1.0.0
     * @apiName PostVerifySend
     * @apiGroup Verify
     *
     * @apiDescription Sends a verification message to the user's mobile phone(s) on record.
     *
     * ~ ~ ~ ~ VERIFY WORKFLOW ~ ~ ~ ~
     *
     * The user verification process is a two-step workflow that is required when a user has been flagged as needing
     * to assert that they are truly in control of their profile. A user might be flagged for verification for any
     * reason at any time. The consumer of this API must be prepared to handle a user verification error response
     * (403 HTTP Status with errorCode 403_verify_user) for any API call made at any time.
     *
     * The verification process requires that the user authenticates with the API using their username and password,
     * and then enters a code that they receive through a pre-approved SMS text message platform (ie mobile phone).
     * Once the user receives the verification code on their mobile device, the app they are interacting with should provide
     * the means to enter the code and pass the code to the POST /verify web service. The /verify service will confirm
     * that the code provided matches the one on file for their device. Once the valid code has been confirmed,
     * then the user can resume making their calls to the API. Until the user verification is completed, the user will
     * not be able to make any API calls other than the 'Verify' API web services.
     *
     * 1) Consumer invokes an API (ie /profile)
     *    * API replies with a 403 HTTP Status
     *    * Response body includes an error code of 403_verify_user
     * 3) Consumer invokes POST /verify/send
     *    * API delivers verification codes to the mobile phones associated with the user's profile
     *    * Response includes a success status code
     * 4) Consumer invokes POST /verify
     *    * API receives the verification code and validates the user
     *    * Response includes a success or failure status code with error message
     * 5) Consumer invokes an API (ie /profile)
     *    * Resumes accessing the API
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse InvalidVerificationPhoneNumberError
     * @apiUse SMSSendError
     **/
    @PreAuthorize('isAuthenticated()')
    @GetMapping('/send')
    ResponseEntity sendVerificationCode() {
        verifyService.sendVerifyCodeToCurrentUser()
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {post} /v1/verify Verify user
     * @apiVersion 1.0.0
     * @apiName PostVerify
     * @apiGroup Verify
     *
     * @apiDescription Validates the given verification code for the currently logged in user and returns a success or
     * failure message to the web service consumer.
     *
     * ~ ~ ~ ~ VERIFY WORKFLOW ~ ~ ~ ~
     *
     * The user verification process is a two-step workflow that is required when a user has been flagged as needing
     * to assert that they are truly in control of their profile. A user might be flagged for verification for any
     * reason at any time. The consumer of this API must be prepared to handle a user verification error response
     * (403 HTTP Status with errorCode 403_verify_user) for any API call made at any time.
     *
     * The verification process requires that the user authenticates with the API using their username and password,
     * and then enters a code that they receive through a pre-approved SMS text message platform (ie mobile phone).
     * Once the user receives the verification code on their mobile device, the app they are interacting with should provide
     * the means to enter the code and pass the code to the POST /verify web service. The /verify service will confirm
     * that the code provided matches the one on file for their device. Once the valid code has been confirmed,
     * then the user can resume making their calls to the API. Until the user verification is completed, the user will
     * not be able to make any API calls other than the 'Verify' API web services.
     *
     * 1) Consumer invokes an API (ie /profile)
     *    * API replies with a 403 HTTP Status
     *    * Response body includes an error code of 403_verify_user
     * 3) Consumer invokes POST /verify/send
     *    * API delivers verification codes to the mobile phones associated with the user's profile
     *    * Response includes a success status code
     * 4) Consumer invokes POST /verify
     *    * API receives the verification code and validates the user
     *    * Response includes a success or failure status code with error message
     * 5) Consumer invokes an API (ie /profile)
     *    * Resumes accessing the API
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} code The code that was delivered to user via the /verify/send method
     *
     * @apiExample {json} Example body:
     * {
     *     "code": "123456"
     * }
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse InvalidVerificationPhoneNumberError
     * @apiUse VerifyCodeExpiredError
     **/
    @PreAuthorize('isAuthenticated()')
    @PostMapping
    ResponseEntity verify(@RequestBody Map body) {
        verifyService.verifyCurrentUser(body.code as String)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

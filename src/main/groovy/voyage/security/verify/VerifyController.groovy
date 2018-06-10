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
package voyage.security.verify

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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
@RequestMapping(path=['/api/v1/verify'], produces = 'application/json')
@Api(tags = 'Verify', description = 'endpoints for performing two-step verification and authorization')
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
    //TODO: Need to get the OAuth2 authorized pieces documented on an API endpoint
    @PreAuthorize('isAuthenticated()')
    @GetMapping('/send')
    @ApiOperation(notes = '${controller.verify.workflow}', value = 'Sends a verification message to the users mobile phone(s) on record')
    ResponseEntity<Void> sendVerificationCode() {
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
    //TODO: Need to get the OAuth2 authorized pieces documented on an API endpoint
    @PreAuthorize('isAuthenticated()')
    @PostMapping
    @ApiOperation(notes = '${controller.verify.workflow}',
            value = 'Validates the given verification code sent to the users mobile phone(s) on record')
    ResponseEntity<Void> verify(@RequestBody VerifyResource verify) {
        verifyService.verifyCurrentUser(verify.code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

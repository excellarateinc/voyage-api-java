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
package voyage.security.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/password', '/api/v1.0/password'])
class PasswordResetController {
    private final PasswordResetService passwordResetService

    @Autowired
    PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService
    }

    /**
     * @api {post} /v1/password/forgot Password Forgot
     * @apiVersion 1.0.0
     * @apiName PasswordForgot
     * @apiGroup User
     *
     * @apiDescription Request 'forgot password' instructions sent to the given email address. A successful response is
     * always provided regardless of a valid email address so that hack attempts have no valid feedback to determine
     * emails that may exist in the database.
     *
     * @apiSampleRequest http://voyage.com/api/v1/password/forgot
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 SUCCESS
     *
     * @apiPermission Anonymous
     *
     * @apiUse UserPasswordForgotRequest
     *
     **/
    @PostMapping('/forgot')
    ResponseEntity forgot(@RequestBody PasswordResetRequest request) {
        passwordResetService.sendApiResetMessage(request.email, request.redirectUri)
        return new ResponseEntity(HttpStatus.OK)
    }

    /**
     * @api {post} /v1/password/reset Password Reset
     * @apiVersion 1.0.0
     * @apiName PasswordReset
     * @apiGroup User
     *
     * @apiDescription Reset the user password using the giving request information
     *
     * @apiSampleRequest http://voyage.com/api/v1/password/reset
     *
     * @apiPermission Anonymous
     *
     * @apiUse UserPasswordResetRequest
     * @apiUse WeakPasswordError
     * @apiUse UserPasswordResetTokenExpiredError
     **/
    @PostMapping('/reset')
    ResponseEntity reset(@RequestBody PasswordResetRequest request) {
        passwordResetService.reset(request.email, request.token, request.password)
        return new ResponseEntity(HttpStatus.OK)
    }
}

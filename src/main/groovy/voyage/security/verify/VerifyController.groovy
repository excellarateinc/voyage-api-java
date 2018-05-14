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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(['/api/v1/verify'])
@Api(tags = 'Verify', description = 'endpoints for performing two-step verification and authorization')
class VerifyController {
    private final VerifyService verifyService

    @Autowired
    VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService
    }

    //TODO: Need to get the OAuth2 authorized pieces documented on an API endpoint
    @PreAuthorize('isAuthenticated()')
    @GetMapping('/send')
    @ApiOperation(notes = '${controller.verify.workflow}', value = 'Sends a verification message to the users mobile phone(s) on record')
    ResponseEntity<Void> sendVerificationCode() {
        verifyService.sendVerifyCodeToCurrentUser()
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    //TODO: Need to get the OAuth2 authorized pieces documented on an API endpoint
    @PreAuthorize('isAuthenticated()')
    @PostMapping
    @ApiOperation(notes = '${controller.verify.workflow}', value = 'Validates the given verification code sent to the users mobile phone(s) on record')
    ResponseEntity<Void> verify(@RequestBody VerifyResource verify) {
        verifyService.verifyCurrentUser(verify.code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

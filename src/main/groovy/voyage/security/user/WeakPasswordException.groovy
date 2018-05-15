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

import org.passay.RuleResultDetail
import org.springframework.http.HttpStatus
import voyage.core.error.AppException
import voyage.core.error.ErrorUtils

class WeakPasswordException extends AppException {
    private static final HTTP_STATUS  = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The password does not meet the minimum requirements.'
    private static final Map<String,String> ERRORS = [
            'INSUFFICIENT_UPPERCASE':'Minimum 1 uppercase character is required.',
            'INSUFFICIENT_SPECIAL':'Minimum 1 special character is required.',
            'INSUFFICIENT_LOWERCASE':'Minimum 1 lowercase character is required.',
            'INSUFFICIENT_DIGIT':'Minimum 1 digit character is required.',
            'TOO_SHORT':'Minimum length of 8 characters.',
            'TOO_LONG':'Maximum length of 100 characters.',
    ]

    WeakPasswordException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    WeakPasswordException(String message) {
        super(HTTP_STATUS, message)
    }

    WeakPasswordException(List<RuleResultDetail> details) {
        super(HTTP_STATUS, DEFAULT_MESSAGE + ' Following are the Password Policy Violations: \n' +
                details?.collect { ERRORS.get(it.errorCode) }?.join('\n'))
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(HTTP_STATUS.value(), 'weak_password')
    }
}

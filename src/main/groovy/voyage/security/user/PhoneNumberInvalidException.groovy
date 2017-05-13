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

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

class PhoneNumberInvalidException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The phone number provided is not recognized.'
    private final String codeExtension

    PhoneNumberInvalidException() {
        this(DEFAULT_MESSAGE)
    }

    PhoneNumberInvalidException(String message) {
        this(message, '')
    }

    PhoneNumberInvalidException(String message, String codeExtension) {
        super(HTTP_STATUS, message)
        this.codeExtension = codeExtension
    }

    @Override
    String getErrorCode() {
        String code = HTTP_STATUS.value() + '_phone_invalid'
        if (codeExtension) {
            code = code + '_' + codeExtension.toLowerCase()
        }
        return code
    }
}

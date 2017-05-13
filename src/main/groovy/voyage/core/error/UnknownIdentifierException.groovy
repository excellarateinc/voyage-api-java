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
package voyage.core.error

import org.springframework.http.HttpStatus

/**
 * For use within the service layer to inform the caller that the given ID was not found within the database. This
 * exception class will be caught by an exception handler (ie DefaultExceptionHandler) and transformed into a 404 Not
 * Found HTTP response.
 *
 * If no message is provided during construction of this class, then the default message will be used and provided back
 * to the web service consumer.
 */
class UnknownIdentifierException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND
    private static final String DEFAULT_MESSAGE = 'Unknown record identifier provided'

    UnknownIdentifierException() {
        this(DEFAULT_MESSAGE)
    }

    UnknownIdentifierException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_unknown_identifier'
    }
}

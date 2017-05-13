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
 * Basic exception that can be thrown by the application and will be caught by the DefaultExceptionHandler. This exception
 * will be translated into a general error back to the API consumer.
 */
class AppException extends RuntimeException {
    private final HttpStatus httpStatus

    AppException() {
        super()
        httpStatus = HttpStatus.BAD_REQUEST
    }

    AppException(HttpStatus httpStatus, String message) {
        super(message)
        this.httpStatus = httpStatus
    }

    HttpStatus getHttpStatus() {
        return httpStatus
    }

    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value())
    }
}


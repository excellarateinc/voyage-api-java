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

class ErrorUtils {
    private static final String UNDER_SCORE = '_'

    static String getErrorCode(int httpStatusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        return getErrorCode(httpStatus.value(), httpStatus.name())
    }

    static String getErrorCode(int httpStatusCode, String description) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        String errorCode = httpStatus.value() + UNDER_SCORE + description
        return formatErrorCode(errorCode)
    }

    static String formatErrorCode(String description) {
        return description.toLowerCase().replace(' ', UNDER_SCORE)
    }
}

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

import spock.lang.Specification

class ErrorUtilsSpec extends Specification {

    def 'getErrorCode returns a properly formatted error code for a 404 status code'() {
        when:
            String errorCode = ErrorUtils.getErrorCode(404)
        then:
            errorCode == '404_not_found'
    }

    def 'getErrorCode returns a properly formatted error code for a 404 status code and custom description'() {
        when:
            String errorCode = ErrorUtils.getErrorCode(404, 'test description')
        then:
            errorCode == '404_test_description'
    }

    def 'formatErrorCode properly handles spaces'() {
        when:
            String errorCode = ErrorUtils.formatErrorCode('test description here')
        then:
            errorCode == 'test_description_here'
    }
}

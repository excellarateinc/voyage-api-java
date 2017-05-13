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

import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DefaultExceptionHandlerSpec extends Specification {
    def 'handle() Exception returns a 500 Internal Server error'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler()
            Exception exception = new Exception('Default message')

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            responseEntity.statusCodeValue == 500
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '500_internal_server_error'
            responseEntity.body[0].errorDescription == 'Unexpected error occurred. Contact technical support for ' +
                    'further assistance should this error continue.'
    }
}

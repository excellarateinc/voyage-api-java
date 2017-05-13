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

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerifyControllerIntegrationSpec extends AuthenticatedIntegrationTest {

    /*
       Run the /verify POST test before the /verify/send because the /send will reset the 'code' with a new value. Since
       the /verify process sends the code to a mobile number, there is no easy way to intercept that code value from an
       integration test.
     */
    def '/api/v1/verify POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            String body = '{"code":"code"}'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/verify POST - Anonymous access denied'() {
        given:
            String body = '{"code":"code"}'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify/send POST - Anonymous access denied'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)
        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/verify/send', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify/send POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)
        when:
            ResponseEntity responseEntity = GET('/api/v1/verify/send', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }
}

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
package voyage.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AbstractIntegrationTest extends Specification {

    @Autowired
    protected TestRestTemplate restTemplate

    @LocalServerPort
    protected int httpPort

    protected <T> ResponseEntity<T> GET(String uri, Class<T> responseType) {
        return GET(uri, null, responseType)
    }

    protected <T> ResponseEntity<T> GET(String uri, HttpEntity httpEntity, Class<T> responseType) {
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> POST(String uri, Class<T> responseType) {
        return restTemplate.postForEntity(uri, null, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> POST(String uri, HttpEntity<?> httpEntity, Class<T> responseType) {
        return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> PUT(String uri, HttpEntity<?> httpEntity, Class<T> responseType) {
        return restTemplate.exchange(uri, HttpMethod.PUT, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> DELETE(String uri, Class<T> responseType) {
        return DELETE(uri, null, responseType)
    }

    protected <T> ResponseEntity<T> DELETE(String uri, HttpEntity httpEntity, Class<T> responseType) {
        return restTemplate.exchange(uri, HttpMethod.DELETE, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> OPTIONS(String uri, HttpEntity<?> httpEntity, Class<T> responseType) {
        return restTemplate.exchange(uri, HttpMethod.OPTIONS, httpEntity, responseType, Collections.EMPTY_MAP)
    }
}

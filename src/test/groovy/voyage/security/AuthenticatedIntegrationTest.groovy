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
package voyage.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import voyage.core.AbstractIntegrationTest

class AuthenticatedIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    protected SuperClient superClient

    @Autowired
    protected StandardClient standardClient

    def setup() {
        SecurityContextHolder.setContext(new TestSecurityContext())
    }

    protected <T> ResponseEntity<T> GET(String uri, Class<T> responseType, TestClient testClient = null) {
        return GET(uri, null, responseType, testClient)
    }

    protected <T> ResponseEntity<T> GET(String uri, HttpEntity httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> POST(String uri, Class<T> responseType) {
        return restTemplate.postForEntity(uri, null, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> POST(String uri, HttpEntity<?> httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> PUT(String uri, HttpEntity<?> httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.PUT, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> DELETE(String uri, Class<T> responseType, TestClient testClient = null) {
        return DELETE(uri, null, responseType, testClient)
    }

    protected <T> ResponseEntity<T> DELETE(String uri, HttpEntity httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.DELETE, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> OPTIONS(String uri, HttpEntity<?> httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.OPTIONS, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> HttpEntity authorize(TestClient testClient, HttpEntity<T> httpEntity = null) {
        HttpHeaders httpHeaders = new HttpHeaders()
        if (httpEntity) {
            // HttpEntity locks existing headers, so convert the unmodifiable set to a modifiable set.
            httpHeaders = enableWrite(httpEntity.headers)
        }
        httpHeaders.add('Authorization', "Bearer ${getAccessToken(testClient)}")

        // Create a new HttpEntity since the given one (if given) is immutable
        if (httpEntity?.body) {
            httpEntity = new HttpEntity<T>(httpEntity.body, httpHeaders)
        } else {
            httpEntity = new HttpEntity<>(httpHeaders)
        }

        return httpEntity
    }

    protected String getAccessToken(TestClient testClient) {
        MultiValueMap<String, String> credentials = new LinkedMultiValueMap<String, String>()
        credentials.set('client_id', testClient.clientId)
        credentials.set('client_secret', testClient.clientSecret)
        credentials.set('grant_type', 'client_credentials')

        HttpHeaders headers = new HttpHeaders()
        headers.add('Content-Type', MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(credentials, headers)

        ResponseEntity<Map> responseEntity = restTemplate
                .withBasicAuth(testClient.clientId, testClient.clientSecret)
                .postForEntity('/oauth/token', httpEntity, Map)

        assert responseEntity.statusCode.value() == 200

        return responseEntity.body.access_token
    }

    protected static HttpHeaders enableWrite(HttpHeaders httpHeaders) {
        Set readOnlyHeaders = httpHeaders.entrySet()
        HttpHeaders writableHeaders = new HttpHeaders()
        for (Map.Entry<String, List<String>> entry : readOnlyHeaders) {
            writableHeaders.put(entry.key, entry.value)
        }
        return writableHeaders
    }
}

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
package voyage.security.audit

import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone

class HttpActionLogFilterIntegrationSpec extends AuthenticatedIntegrationTest {
    @Autowired
    ActionLogRepository actionLogRepository

    def 'Anonymous GET success'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.add('test-header', 'test-value')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            Iterable<ActionLog> actionLogsBefore = actionLogRepository.findAll()
            ResponseEntity<String> responseEntity = GET('/api/status?test=value', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 200
            Iterable<ActionLog> actionLogsAfter = actionLogRepository.findAll()

            Iterable<ActionLog> diff = getNewActionLogs(actionLogsBefore, actionLogsAfter)
            diff.size() == 1

            ActionLog actionLog = diff[0]
            actionLog.url.indexOf('/api/status?test=value') > 0
            actionLog.clientIpAddress == '127.0.0.1'
            actionLog.clientProtocol == 'HTTP/1.1'
            actionLog.httpMethod == 'GET'
            actionLog.httpStatus == '200'
            !actionLog.username
            !actionLog.client
            !actionLog.user
            actionLog.durationMs
            actionLog.requestHeaders ==~ /.*test-header:test-value.*/
            actionLog.requestBody == ''
            actionLog.responseHeaders
            actionLog.responseBody ==~ /\{"status":"alive","datetime":".*"}/
            actionLog.createdDate
            actionLog.lastModifiedDate
    }

    def 'Anonymous POST failure'() {
        given:
            User user = new User(firstName:'TestCORS', lastName:'User', username:'CORS', email:'CORS@email.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.add('test-header', 'test-value')
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            Iterable<ActionLog> actionLogsBefore = actionLogRepository.findAll()
            ResponseEntity<String> responseEntity = POST('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 401
            Iterable<ActionLog> actionLogsAfter = actionLogRepository.findAll()

            Iterable<ActionLog> diff = getNewActionLogs(actionLogsBefore, actionLogsAfter)
            diff.size() == 1

            ActionLog actionLog = diff[0]
            actionLog.url.indexOf('/api/v1/users') > 0
            actionLog.clientIpAddress == '127.0.0.1'
            actionLog.clientProtocol == 'HTTP/1.1'
            actionLog.httpMethod == 'POST'
            actionLog.httpStatus == '401'
            !actionLog.username
            !actionLog.client
            !actionLog.user
            actionLog.durationMs
            actionLog.requestHeaders ==~ /.*test-header:test-value.*/
            actionLog.requestBody == ''
            actionLog.responseHeaders
            actionLog.responseBody == '[{"error":"401_unauthorized","errorDescription":"401 Unauthorized. ' +
                    'Full authentication is required to access this resource"}]'
            actionLog.createdDate
            actionLog.lastModifiedDate
    }

    def 'Login Form POST failure'() {
        given:
            HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build()

            List<NameValuePair> postBody = []
            postBody.add(new BasicNameValuePair('username', 'super'))
            postBody.add(new BasicNameValuePair('password', 'password'))

            HttpPost httpPost = new HttpPost("http://localhost:${httpPort}/login")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))

        when:
            Iterable<ActionLog> actionLogsBefore = actionLogRepository.findAll()
            CloseableHttpResponse response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            Iterable<ActionLog> actionLogsAfter = actionLogRepository.findAll()

            Iterable<ActionLog> diff = getNewActionLogs(actionLogsBefore, actionLogsAfter)
            diff.size() == 1

            ActionLog actionLog = diff[0]
            actionLog.url.indexOf('/login') > 0
            actionLog.clientIpAddress == '127.0.0.1'
            actionLog.clientProtocol == 'HTTP/1.1'
            actionLog.httpMethod == 'POST'
            actionLog.httpStatus == '302'
            actionLog.username == 'super'
            !actionLog.client
            !actionLog.user
            actionLog.durationMs
            actionLog.requestHeaders
            actionLog.requestBody == 'username=super&password=*********'
            actionLog.responseHeaders ==~ '.*Location:http://localhost.*'
            actionLog.responseBody == ''
            actionLog.createdDate
            actionLog.lastModifiedDate
    }

    def 'Super User GET success'() {
        when:
            Iterable<ActionLog> actionLogsBefore = actionLogRepository.findAll()
            ResponseEntity<User> responseEntity = GET('/api/v1/users/1', User, superClient)

        then:
            responseEntity.statusCode.value() == 200
            Iterable<ActionLog> actionLogsAfter = actionLogRepository.findAll()

            Iterable<ActionLog> diff = getNewActionLogs(actionLogsBefore, actionLogsAfter)
            diff.size() == 2

            ActionLog actionLog = diff[0]
            actionLog.url.indexOf('/oauth/token') > 0
            actionLog.clientIpAddress == '127.0.0.1'
            actionLog.clientProtocol == 'HTTP/1.1'
            actionLog.httpMethod == 'POST'
            actionLog.httpStatus == '200'
            actionLog.username == 'client-super'
            actionLog.user.id == 3
            !actionLog.client
            actionLog.durationMs
            actionLog.requestHeaders
            actionLog.requestBody == 'client_id=client-super&client_secret=secret&grant_type=client_credentials'
            actionLog.responseHeaders
            actionLog.responseBody ==~ /.*access_token.*/

            ActionLog actionLog2 = diff[1]
            actionLog2.url.indexOf('/api/v1/users/1') > 0
            actionLog2.clientIpAddress == '127.0.0.1'
            actionLog2.clientProtocol == 'HTTP/1.1'
            actionLog2.httpMethod == 'GET'
            actionLog2.httpStatus == '200'
            actionLog2.username == 'client-super'
            actionLog2.client.id == 1
            actionLog2.user.id == 3
            actionLog2.durationMs
            actionLog2.requestHeaders ==~ /.*authorization:.*/
            actionLog2.requestBody == ''
            actionLog2.responseHeaders
            actionLog2.responseBody == '{"id":1,"firstName":"Super","lastName":"User","username":"super","email":"support@LighthouseSoftware.com",' +
                        '"password":"$2a$10$.Qa2l9VysOeG5M8HhgUbQ.h8KlTBLdMY/slPwMtL/I5OYibYUFQle","isEnabled":true,"isAccountExpired":false,' +
                        '"isAccountLocked":false,"isCredentialsExpired":false,"phones":[{"id":1,"phoneType":"Mobile","phoneNumber":"16518886021"},' +
                        '{"id":2,"phoneType":"Office","phoneNumber":"16518886022"},{"id":3,"phoneType":"Home","phoneNumber":"16518886023"},' +
                        '{"id":4,"phoneType":"Other","phoneNumber":"16518886024"}]}'
            actionLog2.createdDate
            actionLog2.lastModifiedDate
    }

    def 'Super User POST success'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username99', email:'test@test.com', password:'password')
            user.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            Iterable<ActionLog> actionLogsBefore = actionLogRepository.findAll()
            ResponseEntity<User> responseEntity = POST('/api/v1/users', httpEntity, User, superClient)

        then:
            responseEntity.statusCode.value() == 201
            Iterable<ActionLog> actionLogsAfter = actionLogRepository.findAll()

            Iterable<ActionLog> diff = getNewActionLogs(actionLogsBefore, actionLogsAfter)
            diff.size() == 2

            ActionLog actionLog = diff[0]
            actionLog.url.indexOf('/oauth/token') > 0
            actionLog.clientIpAddress == '127.0.0.1'
            actionLog.clientProtocol == 'HTTP/1.1'
            actionLog.httpMethod == 'POST'
            actionLog.httpStatus == '200'
            actionLog.username == 'client-super'
            actionLog.user.id == 3
            !actionLog.client
            actionLog.durationMs
            actionLog.requestHeaders
            actionLog.requestBody == 'client_id=client-super&client_secret=secret&grant_type=client_credentials'
            actionLog.responseHeaders
            actionLog.responseBody ==~ /.*access_token.*/

            ActionLog actionLog2 = diff[1]
            actionLog2.url.indexOf('/api/v1/users') > 0
            actionLog2.clientIpAddress == '127.0.0.1'
            actionLog2.clientProtocol == 'HTTP/1.1'
            actionLog2.httpMethod == 'POST'
            actionLog2.httpStatus == '201'
            actionLog2.username == 'client-super'
            actionLog2.client.id == 1
            actionLog2.user.id == 3
            actionLog2.durationMs
            actionLog2.requestHeaders ==~ /.*authorization:.*/
            actionLog2.requestBody == '{"id":null,"firstName":"Test1","lastName":"User","username":"username99",' +
                    '"email":"test@test.com","password":"*********","isEnabled":true,"isAccountExpired":false,' +
                    '"isAccountLocked":false,"isCredentialsExpired":false,' +
                    '"phones":[{"id":null,"phoneType":"Mobile","phoneNumber":"+1-651-888-6021"}]}'
            actionLog2.responseHeaders ==~ /.*Location:.*/
            actionLog2.responseBody ==~ /.*"username":"username99".*/
            actionLog2.createdDate
            actionLog2.lastModifiedDate
    }

    private static Iterable<ActionLog> getNewActionLogs(Iterable<ActionLog> actionLogsBefore, Iterable<ActionLog> actionLogsAfter) {
        (actionLogsAfter + actionLogsBefore) - actionLogsAfter.intersect(actionLogsBefore)
    }
}

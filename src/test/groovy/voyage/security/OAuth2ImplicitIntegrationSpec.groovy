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

import org.apache.http.Header
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import voyage.security.client.Client
import voyage.security.client.ClientService

/**
 * Exercise the OAuth2 implicit workflow that assumes the client cannot keep the client_secret secure, like in a
 * Javascript web app or mobile app (hybrid or native). This method is the most commonly used authentication method
 * within this app.
 */
class OAuth2ImplicitIntegrationSpec extends AuthenticatedIntegrationTest {

    @Autowired
    ClientService clientService

    def 'OAuth2 implicit authentication with user approval of client scopes returns valid token'() {
        given:
            HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build()

        // Post to /authorize with "implicit" grant and client credentials w/ redirect URI
        // -- expect a redirect to the login page
        // -- expect a JSESSIONID cookie to pass into the next request
        when:
            List<NameValuePair> postBody = []
            postBody.add(new BasicNameValuePair('client_id', superClient.clientId))
            postBody.add(new BasicNameValuePair('redirect_uri', 'http://localhost:8080/oauth'))
            postBody.add(new BasicNameValuePair('response_type', 'token'))

            HttpPost httpPost = new HttpPost("http://localhost:${httpPort}/oauth/authorize")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))

            CloseableHttpResponse response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/login') > 0
            Header sessionCookie = response.getFirstHeader('Set-Cookie')
            sessionCookie.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Post to the /login page (Form post) with user credentials
        // -- set cookie JSESSIONID from prior request
        // -- expect a redirect to the authorize page
        when:
            postBody = []
            postBody.add(new BasicNameValuePair('username', 'super'))
            postBody.add(new BasicNameValuePair('password', 'password'))

            httpPost = new HttpPost("http://localhost:${httpPort}/login")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))
            response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/oauth/authorize') > 0
            Header sessionCookie2 = response.getFirstHeader('Set-Cookie')
            sessionCookie2.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Get the /authorize page (following the redirect response)
        when:
            HttpGet httpGet = new HttpGet("http://localhost:${httpPort}/oauth/authorize")
            response = httpClient.execute(httpGet)

        then:
            response.statusLine.statusCode == 200
            EntityUtils.toString(response.entity).indexOf('Grant Approval') > 0

        // Post to the /authorize page (Form post) with "accepted"
        // -- set cookie JSESSIONID from prior request
        // -- expect a JSON response with a token
        when:
            postBody = []
            postBody.add(new BasicNameValuePair('user_oauth_approval', 'true'))

            httpPost = new HttpPost("http://localhost:${httpPort}/oauth/authorize")

            httpPost.setEntity(new UrlEncodedFormEntity(postBody))
            response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            Header redirectWithToken = response.getFirstHeader('Location')
            redirectWithToken.value.indexOf('http://localhost:8080/oauth#access_token=') >= 0
            response.close()

        // Make a request to an API endpoint using a new Http client (without residual cookies or params) to validate
        // that the given token is legit
        when:
            String accessToken = (redirectWithToken.value =~ /access_token=(.*)&token_type/)[0][1]

            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.add('Authorization', 'Bearer ' + accessToken)
            HttpEntity<Iterable> httpEntity = new HttpEntity<Iterable>(null, httpHeaders)

            ResponseEntity<Iterable> apiResponse = restTemplate.exchange('/api/v1/users', HttpMethod.GET, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
            apiResponse.statusCode.value() == 200
            apiResponse.body[0].id == 1L
            apiResponse.body[0].firstName == 'Super'
            apiResponse.body[0].lastName == 'User'
    }

    def 'OAuth2 implicit authentication without client scope approval returns valid token'() {

        // Post to /authorize with "implicit" grant and client credentials w/ redirect URI
        // -- expect a redirect to the login page

        // Post to the /login page (Form post) with user credentials
        // -- get/set cookie JSESSIONID from prior request
        // -- expect a JSON response with a token

        given:
            // Setup the super client to bypass user approval of the client scopes
            Client clientSuper = clientService.findByClientIdentifier('client-super')
            clientSuper.isAutoApprove = true
            clientService.save(clientSuper)

            HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build()

        // Post to /authorize with "implicit" grant and client credentials w/ redirect URI
        // -- expect a redirect to the login page
        // -- expect a JSESSIONID cookie to pass into the next request
        when:
            List<NameValuePair> postBody = []
            postBody.add(new BasicNameValuePair('client_id', superClient.clientId))
            postBody.add(new BasicNameValuePair('redirect_uri', 'http://localhost:8080/oauth'))
            postBody.add(new BasicNameValuePair('response_type', 'token'))

            HttpPost httpPost = new HttpPost("http://localhost:${httpPort}/oauth/authorize")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))

            CloseableHttpResponse response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/login') > 0
            Header sessionCookie = response.getFirstHeader('Set-Cookie')
            sessionCookie.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Post to the /login page (Form post) with user credentials
        // -- set cookie JSESSIONID from prior request
        // -- expect a redirect to the authorize page
        when:
            postBody = []
            postBody.add(new BasicNameValuePair('username', 'super'))
            postBody.add(new BasicNameValuePair('password', 'password'))

            httpPost = new HttpPost("http://localhost:${httpPort}/login")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))
            response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/oauth/authorize') > 0
            Header sessionCookie2 = response.getFirstHeader('Set-Cookie')
            sessionCookie2.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Get the /authorize page (following the redirect response)
        when:
            HttpGet httpGet = new HttpGet("http://localhost:${httpPort}/oauth/authorize")
            response = httpClient.execute(httpGet)

        then:
            response.statusLine.statusCode == 302
            Header redirectWithToken = response.getFirstHeader('Location')
            redirectWithToken.value.indexOf('http://localhost:8080/oauth#access_token=') >= 0
            response.close()

        // Make a request to an API endpoint using a new Http client (without residual cookies or params) to validate
        // that the given token is legit
        when:
            String accessToken = (redirectWithToken.value =~ /access_token=(.*)&token_type/)[0][1]

            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.add('Authorization', 'Bearer ' + accessToken)
            HttpEntity<Iterable> httpEntity = new HttpEntity<Iterable>(null, httpHeaders)

            ResponseEntity<Iterable> apiResponse = restTemplate.exchange('/api/v1/users', HttpMethod.GET, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
            apiResponse.statusCode.value() == 200
            apiResponse.body[0].id == 1L
            apiResponse.body[0].firstName == 'Super'
            apiResponse.body[0].lastName == 'User'

        cleanup:
            clientSuper.isAutoApprove = false
            clientService.save(clientSuper)
    }
}

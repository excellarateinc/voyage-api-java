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
package voyage.security.client

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import spock.lang.Specification

class ClientServiceSpec extends Specification {
    ClientService clientService
    ClientRepository clientRepository
    OAuth2Request oAuth2Request

    def setup() {
        clientRepository = Mock()

        clientService = new ClientService(clientRepository)

        oAuth2Request = new OAuth2Request(null, '1', null, true, null, null, null, null, null)
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, null)
        SecurityContextHolder.context.setAuthentication(oAuth2Authentication)
    }

    def 'getCurrentClient returns a Client by Authentication clientId'() {
        given:
            Client client = new Client()
        when:
            Client currentClient = clientService.currentClient
        then:
            oAuth2Request.clientId >> '1'
            clientRepository.findByClientIdentifier('1') >> client
            currentClient == client
    }

    def 'getCurrentClient returns null if not an OAuth authentication'() {
        given:
            Authentication authentication = new UsernamePasswordAuthenticationToken(null, null)
            SecurityContextHolder.context.setAuthentication(authentication)
        when:
            Client currentClient = clientService.currentClient
        then:
            !currentClient
    }

    def 'getCurrentClient returns null if no authentication'() {
        given:
            SecurityContextHolder.context.setAuthentication(null)
        when:
            Client currentClient = clientService.currentClient
        then:
            !currentClient
    }

    def 'getPasswordResetRedirectUri returns a URI with null input'() {
        given:
            Client client = new Client()
            client.clientRedirects = [
                new ClientRedirect(clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test'),
            ]
        when:
            String uri = clientService.passwordResetRedirectUri
        then:
            oAuth2Request.clientId >> '1'
            clientRepository.findByClientIdentifier('1') >> client
            uri == 'test'
    }

    def 'getPasswordResetRedirectUri returns a matching URI'() {
        given:
            Client client = new Client()
            client.clientRedirects = [
                new ClientRedirect(id:1, client:client, clientRedirectType:ClientRedirectType.OAUTH, redirectUri:'oauth'),
                new ClientRedirect(id:2, client:client, clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test'),
                new ClientRedirect(id:3, client:client, clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test2'),
            ]
        when:
            String uri = clientService.getPasswordResetRedirectUri('test')
            String uri2 = clientService.getPasswordResetRedirectUri('test2')
        then:
            oAuth2Request.clientId >> '1'
            clientRepository.findByClientIdentifier('1') >> client
            uri == 'test'
            uri2 == 'test2'
    }

    def 'getPasswordResetRedirectUri returns null with no match to URI'() {
        given:
            Client client = new Client()
            client.clientRedirects = [
                new ClientRedirect(clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test'),
            ]
        when:
            String uri = clientService.getPasswordResetRedirectUri('no-match')
        then:
            oAuth2Request.clientId >> '1'
            clientRepository.findByClientIdentifier('1') >> client
            !uri
    }

    def 'getPasswordResetRedirectUri returns the first PASSWORD_RESET URI'() {
        given:
            Client client = new Client()
            client.clientRedirects = [
                new ClientRedirect(clientRedirectType:ClientRedirectType.OAUTH, redirectUri:'oauth'),
                new ClientRedirect(clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test'),
                new ClientRedirect(clientRedirectType:ClientRedirectType.PASSWORD_RESET, redirectUri:'test2'),
            ]
        when:
            String uri = clientService.getPasswordResetRedirectUri('test')
        then:
            oAuth2Request.clientId >> '1'
            clientRepository.findByClientIdentifier('1') >> client
            uri == 'test'
    }
}

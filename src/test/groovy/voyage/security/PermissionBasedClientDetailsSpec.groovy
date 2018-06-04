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

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientGrant
import voyage.security.client.ClientRedirect
import voyage.security.client.ClientRedirectType
import voyage.security.client.ClientScope
import voyage.security.client.ClientScopeType
import voyage.security.client.GrantType

class PermissionBasedClientDetailsSpec extends Specification {

    def 'getScope returns a list of scope types sorted alpha'() {
        given:
            Client client = new Client()
            client.clientScopes = [
                    new ClientScope(clientScopeType:new ClientScopeType(name:'B type')),
                    new ClientScope(clientScopeType:new ClientScopeType(name:'C type')),
                    new ClientScope(clientScopeType:new ClientScopeType(name:'A type')),
            ]
            PermissionBasedClientDetails details = new PermissionBasedClientDetails(client, null)

        when:
            Set<String> scope = details.scope

        then:
            3 == scope.size()
            scope[0] == 'A type'
            scope[1] == 'B type'
            scope[2] == 'C type'
    }

    def 'getAuthorizedGrantTypes returns a list of types sorted alpha'() {
        given:
            Client client = new Client()
            client.clientGrants = [
                    new ClientGrant(grantType:GrantType.CLIENT_CREDENTIALS),
                    new ClientGrant(grantType:GrantType.AUTHORIZATION_CODE),
                    new ClientGrant(grantType:GrantType.PASSWORD),
            ]
            PermissionBasedClientDetails details = new PermissionBasedClientDetails(client, null)

        when:
            Set<String> grants = details.authorizedGrantTypes

        then:
            3 == grants.size()
            grants[0] == 'authorization_code'
            grants[1] == 'client_credentials'
            grants[2] == 'password'
    }

    def 'getRegisteredRedirectUri returns a list of URIs sorted alpha'() {
        given:
            Client client = new Client()
            client.clientRedirects = [
                    new ClientRedirect(id:1, client:client, redirectUri:'test1', clientRedirectType:ClientRedirectType.OAUTH),
                    new ClientRedirect(id:2, client:client, redirectUri:'test2', clientRedirectType:ClientRedirectType.PASSWORD_RESET),
                    new ClientRedirect(id:3, client:client, redirectUri:'test3', clientRedirectType:ClientRedirectType.OAUTH),
            ]
            PermissionBasedClientDetails details = new PermissionBasedClientDetails(client, null)

        when:
            Set<String> redirects = details.registeredRedirectUri

        then:
            2 == redirects.size()
            redirects[0] == 'test1'
            redirects[1] == 'test3'
    }
}

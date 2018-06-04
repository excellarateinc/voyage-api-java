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

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import voyage.security.client.Client
import voyage.security.client.ClientRedirectType

class PermissionBasedClientDetails implements ClientDetails {
    private final Client client
    private final Collection<GrantedAuthority> authorities

    PermissionBasedClientDetails(Client client, Collection<GrantedAuthority> authorities) {
        this.client = client
        this.authorities = authorities
    }

    @Override
    String getClientId() {
        return client.clientIdentifier
    }

    @Override
    Set<String> getResourceIds() {
        return Collections.EMPTY_SET
    }

    @Override
    boolean isSecretRequired() {
        return client.isSecretRequired
    }

    @Override
    String getClientSecret() {
        return client.clientSecret
    }

    @Override
    boolean isScoped() {
        return client.isScoped
    }

    @Override
    Set<String> getScope() {
        Set<String> scopes = []
        client.clientScopes?.each { clientScope ->
            scopes << clientScope.clientScopeType.name
        }
        return scopes.toSorted { a, b -> a <=> b }
    }

    @Override
    Set<String> getAuthorizedGrantTypes() {
        Set<String> grants = []
        client.clientGrants?.each { clientGrantType ->
            grants << clientGrantType.grantType.code
        }
        return grants.toSorted { a, b -> a <=> b }
    }

    @Override
    Set<String> getRegisteredRedirectUri() {
        Set<String> uris = []
        client.clientRedirects?.each { clientRedirect ->
            if (ClientRedirectType.OAUTH == clientRedirect.clientRedirectType) {
                uris << clientRedirect.redirectUri
            }
        }
        return uris
    }

    @Override
    Collection<GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    Integer getAccessTokenValiditySeconds() {
        return client.accessTokenValiditySeconds
    }

    @Override
    Integer getRefreshTokenValiditySeconds() {
        return client.refreshTokenValiditySeconds
    }

    @Override
    boolean isAutoApprove(String scope) {
        return client.isAutoApprove
    }

    @Override
    Map<String, Object> getAdditionalInformation() {
        return Collections.EMPTY_MAP
    }
}

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

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class ClientService {
    private final ClientRepository clientRepository

    ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository
    }

    Client getCurrentClient() {
        String clientId
        Authentication authentication = SecurityContextHolder.context.authentication
        if (authentication && authentication instanceof OAuth2Authentication) {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication
            clientId = oAuth2Authentication.OAuth2Request.clientId
        }

        if (clientId) {
            return findByClientIdentifier(clientId)
        }

        return null
    }

    Client findByClientIdentifier(@NotNull String clientIdentifier) {
        return clientRepository.findByClientIdentifier(clientIdentifier)
    }

    Client save(@Valid Client client) {
        clientRepository.save(client)
    }

    String getPasswordResetRedirectUri(String passwordResetUri = null) {
        Client client = currentClient
        Set<ClientRedirect> passwordClientRedirects = client?.clientRedirects?.findAll {
            if (ClientRedirectType.PASSWORD_RESET == it.clientRedirectType) {
                if (passwordResetUri && passwordResetUri == it.redirectUri) {
                    return true
                } else if (!passwordResetUri) {
                    return true
                }
            }
            return false
        }

        if (passwordClientRedirects) {
            return passwordClientRedirects.first().redirectUri
        }

        return null
    }
}

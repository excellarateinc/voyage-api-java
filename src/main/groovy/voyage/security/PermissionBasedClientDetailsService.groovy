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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.permission.Permission
import voyage.security.permission.PermissionService

@Service
@Transactional(readOnly = true)
class PermissionBasedClientDetailsService implements ClientDetailsService {
    private final ClientService clientService
    private final PermissionService permissionService

    PermissionBasedClientDetailsService(ClientService clientService, PermissionService permissionService) {
        this.clientService = clientService
        this.permissionService = permissionService
    }

    @Override
    ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientService.findByClientIdentifier(clientId)
        if (!client || !client.isEnabled) {
            throw new ClientRegistrationException('No client was found for the given username and password')
        } else if (client.isAccountLocked) {
            throw new ClientRegistrationException('The client account is locked')
        }
        return new PermissionBasedClientDetails(client, getAuthorities(client))
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Client client) {
        Collection<SimpleGrantedAuthority> authorities = [] as Set<SimpleGrantedAuthority>
        Iterable<Permission> permissions = permissionService.findAllByClient(client.id)
        permissions?.each { permission ->
            authorities.add(new SimpleGrantedAuthority(permission.name))
        }
        return authorities
    }
}

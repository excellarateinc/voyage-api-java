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
import org.springframework.security.core.userdetails.UserDetails
import voyage.security.user.User

class PermissionBasedUserDetails implements UserDetails {
    private final User user
    private final Set<GrantedAuthority> authorities

    PermissionBasedUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user
        this.authorities = authorities
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    String getPassword() {
        return user.password
    }

    @Override
    String getUsername() {
        return user.username
    }

    @Override
    boolean isAccountNonExpired() {
        return !user.isAccountExpired
    }

    @Override
    boolean isAccountNonLocked() {
        return !user.isAccountLocked
    }

    @Override
    boolean isCredentialsNonExpired() {
        return !user.isCredentialsExpired
    }

    @Override
    boolean isEnabled() {
        return user.isEnabled
    }
}

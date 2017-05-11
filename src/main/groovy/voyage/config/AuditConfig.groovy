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
package voyage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

@Configuration
@EnableJpaAuditing
class AuditConfig {

    @Bean
    AuditorAware<String> getAuditorProvider() {
        return new SpringSecurityAuditorAware()
    }

    class SpringSecurityAuditorAware implements AuditorAware<String> {
        String getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.context.authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return 'SYSTEM'
            }
            if (authentication.principal instanceof User) {
                return ((User)authentication.principal).username
            }
            return authentication.principal
        }
    }
}

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
package voyage.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import org.hibernate.validator.constraints.NotBlank
import voyage.security.audit.AuditableEntity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['phoneNumber'], callSuper=true)
class UserPhone extends AuditableEntity {
    @Enumerated(EnumType.STRING)
    PhoneType phoneType

    @NotBlank
    String phoneNumber

    @JsonIgnore
    String verifyCode

    @NotNull
    @JsonIgnore
    Boolean isValidated = Boolean.FALSE

    @JsonIgnore
    Date verifyCodeExpiresOn

    @ManyToOne
    @JoinColumn(name='user_id')
    @JsonIgnore
    User user

    @JsonIgnore
    boolean isVerifyCodeExpired() {
        return verifyCodeExpiresOn != null && verifyCodeExpiresOn < new Date()
    }
}

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
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import voyage.security.audit.AuditableEntity
import voyage.security.role.Role

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Transient
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['firstName', 'lastName', 'username'], callSuper=true)
class User extends AuditableEntity {
    @Transient
    private String oldUsername

    @Transient
    private String oldPassword

    @NotBlank
    String firstName

    @NotBlank
    String lastName

    @Email
    String email

    @NotBlank
    String username

    @NotBlank
    String password

    @JsonIgnore
    Date passwordCreatedDate

    @JsonIgnore
    String passwordResetToken

    @JsonIgnore
    Date passwordResetDate

    @JsonIgnore
    String passwordResetLoginUri

    @NotNull
    Boolean isEnabled = Boolean.TRUE

    @NotNull
    Boolean isAccountExpired = Boolean.FALSE

    @NotNull
    Boolean isAccountLocked = Boolean.FALSE

    @NotNull
    Boolean isCredentialsExpired = Boolean.FALSE

    @NotNull
    @JsonIgnore
    Boolean isVerifyRequired = Boolean.FALSE

    @JsonIgnore
    Integer failedLoginAttempts

    /**
     * Force all tokens for this client created on or before this date to be expired even if the original token has not
     * yet expired.
     */
    @JsonIgnore
    Date forceTokensExpiredDate

    @ManyToMany
    @JoinTable(name='user_role', joinColumns=@JoinColumn(name='user_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles

    @OneToMany(fetch=FetchType.EAGER, mappedBy='user', cascade=CascadeType.ALL)
    @Where(clause = 'is_deleted = 0')
    Set<UserPhone> phones

    void setUsername(String newUsername) {
        oldUsername = username
        username = newUsername
    }

    @JsonIgnore
    boolean isNewUsername() {
        // Account for when hibernate loads an object from the database. Only track changes after the load.
        if (id && oldUsername != null && oldUsername != username) {
            return true
        } else if (!id && username) {
            return true
        }
        return false
    }

    void setPassword(String newPassword) {
        oldPassword = password
        password = newPassword
    }

    @JsonIgnore
    boolean isNewPassword() {
        // Account for when hibernate loads an object from the database. Only track changes after the load.
        if (id && oldPassword != null && oldPassword != password) {
            return true
        } else if (!id && password) {
            return true
        }
        return false
    }
}

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

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity
import voyage.security.role.Role

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['name', 'clientIdentifier'], callSuper=true)
class Client extends AuditableEntity {
    /**
     * The client name
     */
    @NotNull
    String name

    /**
     * The client equivalent of the user "username". The identifier given when the client is identifying itself to the
     * app. This field is referred to as the "client_id" in OAuth2 specs. This object uses "client_identifier" so that
     * it is not confused with the foreign key column naming pattern "client_id" within the database.
     */
    @NotNull
    String clientIdentifier

    /**
     * The client password. This should be kept secret and only used in a server-to-server authentication process.
     */
    @NotNull
    String clientSecret

    /**
     * Flag to enforce that the client secret is passed in during authentication. This should only be true for clients
     * that will be doing server-to-server communication.
     */
    @NotNull
    Boolean isSecretRequired = Boolean.FALSE

    /**
     * Flag to limit the client to a specific scope. If false, the scope of the authentication request will be ignored.
     */
    @NotNull
    Boolean isScoped = Boolean.FALSE

    /**
     * Flag to skip the user approval page where the client "scopes" are displayed with an "Approve" or "Deny" option.
     * isAutoApprove = true will skip the user approval page
     * isAutoApprove = false will force the user approval page to be displayed
     */
    @NotNull
    Boolean isAutoApprove = Boolean.TRUE

    /**
     * Flag to enable or disable the client
     */
    @NotNull
    Boolean isEnabled = Boolean.TRUE

    /**
     * Flag that indicates if the client account is locked, usually due to too many failed login attempts
     */
    @NotNull
    Boolean isAccountLocked = Boolean.FALSE

    /**
     * The number of seconds before the access token for the client is expired.
     */
    @NotNull
    Integer accessTokenValiditySeconds = 7200 // 2 Hours

    /**
     * The number of seconds before the refresh token issued to the client is expired. Refresh tokens should be much
     * longer than the access tokens.
     */
    @NotNull
    Integer refreshTokenValiditySeconds = 86400 // 24 hours

    /**
     * Force all tokens for this client created on or before this date to be expired even if the original token has not
     * yet expired.
     */
    @JsonIgnore
    Date forceTokensExpiredDate

    /**
     * The number of failed login attempts since the last successful login
     */
    @JsonIgnore
    Integer failedLoginAttempts

    /**
     * The authentication "grant" types that this client is allowed to request.
     *
     * Grant types include: client_credentials, authorization_code, password, implicit
     *
     * See the OAuth2 specs for more information on these types at http://oauth.com
     */
    @OneToMany(fetch=FetchType.EAGER, mappedBy='client')
    @Where(clause = 'is_deleted = 0')
    Set<ClientGrant> clientGrants

    /**
     * Scopes that the client is allowed to perform.
     *
     * Scope examples could be: Read Your Contacts, Scan Your Inbox, Send Email To Your Mother
     */
    @OneToMany(fetch=FetchType.EAGER, mappedBy='client')
    @Where(clause = 'is_deleted = 0')
    Set<ClientScope> clientScopes

    /**
     * A list of approved redirect URIs for this client. When passing sensitive information back to a client (ie access
     * token), the data is appended to the redirect URI and the response is redirected to the redirect URI.
     *
     * Multiple redirect URIs are supported because the client is required to provide a redirect URI in requests that
     * require a redirect. If the given redirect URI exactly matches a URI in the database for the client, then the
     * given URI will be used for the redirect process.
     */
    @OneToMany(fetch=FetchType.EAGER, mappedBy='client')
    @Where(clause = 'is_deleted = 0')
    Set<ClientRedirect> clientRedirects

    /**
     * A list of approved HTTP Request Origin URIs for this client. Any HTTP request that has an Origin request header
     * will be validated against the current authenticated client's Origin list. If the given Origin matches an Origin
     * in the client's origin list, then the proper HTTP response headers will be returned. If no origin matches, then
     * a public '*' response will be given.
     */
    @OneToMany(fetch=FetchType.EAGER, mappedBy='client')
    @Where(clause = 'is_deleted = 0')
    Set<ClientOrigin> clientOrigins

    /**
     * The roles that the client has been granted for accessing resources within the app
     */
    @ManyToMany
    @JoinTable(name='client_role', joinColumns=@JoinColumn(name='client_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles
}

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
package voyage.security.audit

import groovy.transform.EqualsAndHashCode
import voyage.security.client.Client
import voyage.security.user.User

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@EqualsAndHashCode
class ActionLog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id

    @NotNull
    String clientIpAddress

    @NotNull
    String clientProtocol

    @NotNull
    String url

    @NotNull
    String httpMethod

    String httpStatus

    String username

    @ManyToOne
    @JoinColumn(name='user_id')
    User user

    @ManyToOne
    @JoinColumn(name='client_id')
    Client client

    Long durationMs

    @Lob
    String requestHeaders

    @Lob
    String requestBody

    @Lob
    String responseHeaders

    @Lob
    String responseBody

    @NotNull
    Date createdDate

    Date lastModifiedDate
}

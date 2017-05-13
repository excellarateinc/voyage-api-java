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

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

@MappedSuperclass
@EntityListeners(AuditingEntityListener)
@EqualsAndHashCode(includes=['id'])
class AuditableEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @CreatedBy
    @Audited
    @JsonIgnore
    String createdBy

    @CreatedDate
    @Audited
    @JsonIgnore
    Date createdDate

    @LastModifiedBy
    @Audited
    @JsonIgnore
    String lastModifiedBy

    @LastModifiedDate
    @Audited
    @JsonIgnore
    Date lastModifiedDate

    @NotNull
    @Audited
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE
}

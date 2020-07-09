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

import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient
import java.text.DateFormat

/**
 * Hibernate Envers extension class that adds the username to the revision transaction table
 */
@Entity
@Table(name='AUD_REVISION')
@RevisionEntity(UserRevisionListener)
class UserRevisionEntity implements Serializable {
    private static final long serialVersionUID = -8044692553450000776L

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @RevisionNumber
    int id

    @RevisionTimestamp
    long timestamp

    String username
    Date createdDate

    @Transient
    Date getRevisionDate() {
        return new Date( timestamp )
    }

    @Override
    boolean equals(Object o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        UserRevisionEntity that = (UserRevisionEntity) o

        if (id != that.id) {
            return false
        }
        return (timestamp != that.timestamp)
    }

    @Override
    int hashCode() {
        int result
        result = id
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32))
        return result
    }

    @Override
    String toString() {
        return "DefaultRevisionEntity(id = ${id}, revisionDate = ${DateFormat.dateTimeInstance.format(revisionDate)} )"
    }
}

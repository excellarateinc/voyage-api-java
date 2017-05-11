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

import org.hibernate.envers.RevisionListener
import voyage.security.user.UserService

/**
 * Hibernate Envers - RevisionListener override that looks up the User within the current security context and injects
 * the value into the RevisionEntity object.
 */
class UserRevisionListener implements RevisionListener {
    @Override
    void newRevision(Object revisionEntity) {
        UserRevisionEntity userRevisionEntity = (UserRevisionEntity)revisionEntity
        userRevisionEntity.username = UserService.currentUsername
        userRevisionEntity.createdDate = new Date()
    }
}

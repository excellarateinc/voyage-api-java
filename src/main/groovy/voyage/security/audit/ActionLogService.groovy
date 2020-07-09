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

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.core.error.UnknownIdentifierException

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class ActionLogService {
    private final ActionLogRepository actionLogRepository

    ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository
    }

    ActionLog get(@NotNull Long id) {
        ActionLog actionLog = actionLogRepository.findById(id).orElse(null)
        if (!actionLog) {
            throw new UnknownIdentifierException()
        }
        return actionLog
    }

    ActionLog saveDetached(@Valid ActionLog actionLog) {
        if (actionLog.id) {
            ActionLog existingActionLog = get(actionLog.id)
            existingActionLog.with {
                clientIpAddress = actionLog.clientIpAddress
                clientProtocol = actionLog.clientProtocol
                durationMs = actionLog.durationMs
                httpMethod = actionLog.httpMethod
                httpStatus = actionLog.httpStatus
                username = actionLog.username
                client = actionLog.client
                user = actionLog.user
                requestHeaders = actionLog.requestHeaders
                requestBody = actionLog.requestBody
                responseHeaders = actionLog.responseHeaders
                responseBody = actionLog.responseBody
                url = actionLog.url
                lastModifiedDate = new Date()
            }
            return actionLogRepository.save(existingActionLog)
        }
        actionLog.lastModifiedDate = new Date()
        return actionLogRepository.save(actionLog)
    }
}

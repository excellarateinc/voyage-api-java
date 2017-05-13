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

import spock.lang.Specification
import voyage.core.error.UnknownIdentifierException

class ActionLogServiceSpec extends Specification {
    ActionLog actionLog
    ActionLog modifiedActionLog
    ActionLogRepository actionLogRepository = Mock()
    ActionLogService actionLogService = new ActionLogService(actionLogRepository)

    def setup() {
        actionLog = new ActionLog(clientProtocol:'http', clientIpAddress:'127.0.0.1', httpMethod:'GET', url:'http://test.request.url')
        modifiedActionLog = new ActionLog(clientProtocol:'http', clientIpAddress:'127.0.0.1', httpMethod:'GET', url:'http://test.request.url')
    }

    def 'get - calls the actionLogRepository.findOne' () {
        setup:
            actionLogRepository.findOne(_) >> actionLog
        when:
            ActionLog fetchedActionLog = actionLogService.get(1)
        then:
            'http' == fetchedActionLog.clientProtocol
            '127.0.0.1' == fetchedActionLog.clientIpAddress
    }

    def 'get - UnknownIdentifierException' () {
        setup:
            actionLogRepository.findOne(_) >> null
        when:
            actionLogService.get(1)
        then:
            thrown(UnknownIdentifierException)
    }
}

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
package voyage.security.bfa

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.stereotype.Component

import java.security.SecureRandom

@Component
class SleepAfterFailureEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(SleepAfterFailureEventListener)

    @Value('${security.brute-force-attack.sleep-after-failure.enabled}')
    private boolean isEnabled

    @Value('${security.brute-force-attack.sleep-after-failure.min-sleep-seconds}')
    private int minSleepSeconds

    @Value('${security.brute-force-attack.sleep-after-failure.max-sleep-seconds}')
    private int maxSleepSeconds

    @EventListener
    void authenticationFailed(AbstractAuthenticationFailureEvent ignore) {
        if (!isEnabled) {
            LOG.debug('SleepAfterFailureEventListener is DISABLED. Skipping.')
            return
        }
        LOG.debug('User authentication failed. Sleeping the thread to slow down brute force attacks')

        SecureRandom random = new SecureRandom()
        int sleepSeconds = random.nextInt(maxSleepSeconds)
        if (sleepSeconds < minSleepSeconds) {
            sleepSeconds = minSleepSeconds
        }

        if (LOG.debugEnabled) {
            LOG.debug("Sleeping the thread for ${sleepSeconds} seconds")
        }

        sleep(sleepSeconds * 1000)

        LOG.debug('Resuming the thread')
    }
}

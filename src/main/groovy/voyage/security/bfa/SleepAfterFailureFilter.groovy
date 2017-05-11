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
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom

@Component
@Order(-10001)
class SleepAfterFailureFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(SleepAfterFailureFilter)

    @Value('${security.brute-force-attack.sleep-after-failure.enabled}')
    private boolean isEnabled

    @Value('${security.brute-force-attack.sleep-after-failure.http-status-failure-list}')
    private int[] httpStatusList

    @Value('${security.brute-force-attack.sleep-after-failure.min-sleep-seconds}')
    private int minSleepSeconds

    @Value('${security.brute-force-attack.sleep-after-failure.max-sleep-seconds}')
    private int maxSleepSeconds

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isEnabled) {
            LOG.debug('SleepAfterFailureFilter is DISABLED. Skipping.')
        }

        filterChain.doFilter(request, response)

        if (isEnabled) {
            sleepAfterFailureHttpStatus(response)
        }
    }

    private void sleepAfterFailureHttpStatus(HttpServletResponse response) {
        LOG.debug('Examining the response for failure status codes...')
        if (LOG.debugEnabled) {
            LOG.debug("Found HTTP Status: ${response.status}")
        }

        if (httpStatusList.contains(response.status)) {
            if (LOG.debugEnabled) {
                LOG.debug("${response.status} is a match in the list ${httpStatusList}")
            }

            SecureRandom random = new SecureRandom()
            int sleepSeconds = random.nextInt(maxSleepSeconds)
            if (sleepSeconds < minSleepSeconds) {
                sleepSeconds = minSleepSeconds
            }

            if (LOG.debugEnabled) {
                LOG.debug("Sleeping the thread for ${sleepSeconds} seconds")
            }

            sleep(sleepSeconds * 1000)

            LOG.debug('Resuming the HTTP response filter chain')

        } else {
            LOG.debug('HTTP Status code does not match. Skipping request.')
        }
    }
}

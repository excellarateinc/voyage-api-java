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

import org.springframework.util.StopWatch
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SleepAfterFailureFilterSpec extends Specification {
    long minSleepMillis = 3000
    long maxSleepMillis = 5000

    SleepAfterFailureFilter filter

    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain

    def setup() {
        filter = new SleepAfterFailureFilter()
        filter.isEnabled = true
        filter.httpStatusList = [401, 403, 404]
        filter.minSleepSeconds = minSleepMillis / 1000
        filter.maxSleepSeconds = maxSleepMillis / 1000

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
    }

    def 'doFilterInternal is skipped if disabled'() {
        given:
            filter.isEnabled = false
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            1 * filterChain.doFilter(request, response)

            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'doFilterInternal is skipped if the HTTP response status code does not match'() {
        given:
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            2 * response.status >> 200
            1 * filterChain.doFilter(request, response)

            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'doFilterInternal is executed if the HTTP response status code matches'() {
        given:
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            3 * response.status >> 401
            1 * filterChain.doFilter(request, response)

            withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    private boolean withinSleepRange(long milliseconds) {
        return milliseconds >= minSleepMillis && milliseconds <= maxSleepMillis
    }
}

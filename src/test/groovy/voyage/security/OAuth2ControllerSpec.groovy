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
package voyage.security

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.web.servlet.ModelAndView
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class OAuth2ControllerSpec extends Specification {

    def 'handleError() with OAuth2Exception returns an HTML escaped summary'() {
        given:
            OAuth2Exception exception = new OAuth2Exception('test message')
            def httpServletRequest = Mock(HttpServletRequest)

            OAuth2Controller controller = new OAuth2Controller()

        when:
            ModelAndView modelAndView = controller.handleError(httpServletRequest)

        then:
            httpServletRequest.getAttribute('error') >> exception

            modelAndView.viewName == 'oauth-error'
            modelAndView.model.errorSummary == 'error=&quot;invalid_request&quot;, error_description=&quot;test message&quot;'
    }

    def 'handleError() with Exception returns an error summary'() {
        given:
            Exception exception = new Exception('test message')
            def httpServletRequest = Mock(HttpServletRequest)

            OAuth2Controller controller = new OAuth2Controller()

        when:
            ModelAndView modelAndView = controller.handleError(httpServletRequest)

        then:
            httpServletRequest.getAttribute('error') >> exception

            modelAndView.viewName == 'oauth-error'
            modelAndView.model.errorSummary == 'test message'
    }

    def 'handleError() with String error returns an error summary'() {
        given:
            def httpServletRequest = Mock(HttpServletRequest)

            OAuth2Controller controller = new OAuth2Controller()

        when:
            ModelAndView modelAndView = controller.handleError(httpServletRequest)

        then:
            httpServletRequest.getAttribute('error') >> 'test message'

            modelAndView.viewName == 'oauth-error'
            modelAndView.model.errorSummary == 'test message'
    }
}

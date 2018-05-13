/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.HtmlUtils

import javax.servlet.http.HttpServletRequest

@Controller
@SessionAttributes('authorizationRequest')
class OAuth2Controller {

    @RequestMapping('/oauth/confirm_access')
    ModelAndView getAccessConfirmation(Map<String, Object> model) throws Exception {
        return new ModelAndView('oauth-confirm-access', model)
    }

    @RequestMapping('/oauth/error')
    ModelAndView handleError(HttpServletRequest request) {
        Map<String, Object> model = [:]
        Object error = request.getAttribute('error')
        // The error summary may contain malicious user input, it needs to be escaped to prevent XSS
        String errorSummary
        if (error instanceof OAuth2Exception) {
            OAuth2Exception oauthError = (OAuth2Exception)error
            errorSummary = HtmlUtils.htmlEscape(oauthError.summary)
        } else if (error instanceof Exception) {
            Exception oauthError = (Exception)error
            errorSummary = oauthError.message
        } else {
            errorSummary = error.toString()
        }
        model.put('errorSummary', errorSummary)
        return new ModelAndView('oauth-error', model)
    }
}

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
package voyage.security.filter

import spock.lang.Specification
import voyage.security.filter.XssFilter.XssRequestWrapper

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class XssFilterSpec extends Specification {
    HttpServletRequest request
    HttpServletResponse response
    XssRequestWrapper xssRequestWrapper

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        xssRequestWrapper = new XssFilter().getXssRequestWrapperInstance(request)
    }

    def 'filter wraps the incoming request with a XSSRequestWrapper'() {
        given:
            XssFilter filter = new XssFilter()
            FilterChain filterChain = Mock(FilterChain)

        when:
            filter.doFilterInternal(request, response, filterChain)

        then:
            1 * filterChain.doFilter(*_) >> { args ->
                assert args[0] instanceof XssRequestWrapper
            }
    }

    def 'filter returns decoded HTML to verify that the HTML input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('html')
            String header = xssRequestWrapper.getHeader('html')
            String[] values = xssRequestWrapper.getParameterValues('html')

        then:
            1 * request.getParameter('html') >> '<html><body><h1>Hello > see this &amp; </h1></body></html>'
            1 * request.getHeader('html') >> '<html><body><h1>Hello > see this &amp; </h1></body></html>'
            1 * request.getParameterValues('html') >> ['<html><body><h1>Hello > see this &amp; </h1></body></html>',
                                                             '<html><body><h1>Hello > see this &amp; </h1></body></html>']
            param == '<html><body><h1>Hello > see this & </h1></body></html>'
            header == '<html><body><h1>Hello > see this & </h1></body></html>'
            values[0] == '<html><body><h1>Hello > see this & </h1></body></html>'
            values[1] == '<html><body><h1>Hello > see this & </h1></body></html>'
    }

    def 'filter returns decoded CSS to verify that the CSS input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('css')
            String header = xssRequestWrapper.getHeader('css')
            String[] values = xssRequestWrapper.getParameterValues('css')

        then:
            1 * request.getParameter('css') >> '.test {\\26 \\" \\26}'
            1 * request.getHeader('css') >> '.test {\\26 \\" \\26}'
            1 * request.getParameterValues('css') >> ['.test {\\26 \\" \\26}', '.test {\26 \" \26}']
            param == '.test {\26 " \26}'
            header == '.test {\26 " \26}'
            values[0] == '.test {\26 " \26}'
            values[1] == '.test {\26 " \26}'
    }

    def 'filter returns decoded URI to verify that the URI input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('uri')
            String header = xssRequestWrapper.getHeader('uri')
            String[] values = xssRequestWrapper.getParameterValues('uri')

        then:
            1 * request.getParameter('uri') >> '/test?param=%41%42%43%31%32%33'
            1 * request.getHeader('uri') >> '/test?param=%41%42%43%31%32%33'
            1 * request.getParameterValues('uri') >> ['/test?param=%41%42%43%31%32%33', '/test?param=%41%42%43%31%32%33']
            param == '/test?param=ABC123'
            header == '/test?param=ABC123'
            values[0] == '/test?param=ABC123'
            values[1] == '/test?param=ABC123'
    }

    def 'filter returns decoded Javascript to verify that the Javascript input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('js')
            String header = xssRequestWrapper.getHeader('js')
            String[] values = xssRequestWrapper.getParameterValues('js')

        then:
            1 * request.getParameter('js') >> 'if (\\\'1\\\' == \'1\' || \\"1\\" == "1")'
            1 * request.getHeader('js') >> 'if (\\\'1\\\' == \'1\' || \\"1\\" == "1")'
            1 * request.getParameterValues('js') >> ['if (\\\'1\\\' == \'1\' || \\"1\\" == "1")']
            param == 'if (\'1\' == \'1\' || "1" == "1")'
            header == 'if (\'1\' == \'1\' || "1" == "1")'
            values[0] == 'if (\'1\' == \'1\' || "1" == "1")'
    }

    def 'filter returns decoded VBScript to verify that the VBScript input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('vbs')
            String header = xssRequestWrapper.getHeader('vbs')
            String[] values = xssRequestWrapper.getParameterValues('vbs')

        then:
            1 * request.getParameter('vbs') >> '\\" something here'
            1 * request.getHeader('vbs') >> '\\" something here'
            1 * request.getParameterValues('vbs') >> ['\\" something here']
            param == '" something here'
            header == '" something here'
            values[0] == '" something here'
    }

    def 'filter returns decoded XML to verify that the XML input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('xml')
            String header = xssRequestWrapper.getHeader('xml')
            String[] values = xssRequestWrapper.getParameterValues('xml')

        then:
            1 * request.getParameter('xml') >> '<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>'
            1 * request.getHeader('xml') >> '<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>'
            1 * request.getParameterValues('xml') >> ['<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>']
            param == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'
            header == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'
            values[0] == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'
    }

    def 'filter does not remove normal text'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'This is normal text < with some & symbols that are acceptable.'
            1 * request.getHeader('script') >> 'This is normal text < with some & symbols that are acceptable.'
            1 * request.getParameterValues('script') >> ['This is normal text < with some & symbols that are acceptable.']
            param == 'This is normal text < with some & symbols that are acceptable.'
            header == 'This is normal text < with some & symbols that are acceptable.'
            values[0] == 'This is normal text < with some & symbols that are acceptable.'
    }

    def 'filter removes suspicious scripting: <script>(.*?)</script>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> '<script>(.*?)</script>'
            1 * request.getHeader('script') >> '<script>(.*?)</script>'
            1 * request.getParameterValues('script') >> ['<script>(.*?)</script>']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: src[\\r\\n]*=[\\r\\n]*\'(.*?)\''() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'src=\'something\''
            1 * request.getHeader('script') >> 'src=\'something\''
            1 * request.getParameterValues('script') >> ['src=\'something\'']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: src[\\r\\n]*=[\\r\\n]*"(.*?)"'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'src="something"'
            1 * request.getHeader('script') >> 'src="something"'
            1 * request.getParameterValues('script') >> ['src="something"']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: </script>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> '</script>'
            1 * request.getHeader('script') >> '</script>'
            1 * request.getParameterValues('script') >> ['</script>']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: <script(.*?)>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> '<script(.*?)>'
            1 * request.getHeader('script') >> '<script(.*?)>'
            1 * request.getParameterValues('script') >> ['<script(.*?)>']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: eval\\((.*?)\\)'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'eval(something)'
            1 * request.getHeader('script') >> 'eval(something)'
            1 * request.getParameterValues('script') >> ['eval(something)']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: expression\\((.*?)\\)'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'expression(something)'
            1 * request.getHeader('script') >> 'expression(something)'
            1 * request.getParameterValues('script') >> ['expression(something)']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: javascript:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'javascript:'
            1 * request.getHeader('script') >> 'javascript:'
            1 * request.getParameterValues('script') >> ['javascript:']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: vbscript:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'vbscript:'
            1 * request.getHeader('script') >> 'vbscript:'
            1 * request.getParameterValues('script') >> ['vbscript:']
            param == ''
            header == ''
            values[0] == ''
    }

    def 'filter removes suspicious scripting: onload:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')
            String[] values = xssRequestWrapper.getParameterValues('script')

        then:
            1 * request.getParameter('script') >> 'onload="some function"'
            1 * request.getHeader('script') >> 'onload="some function"'
            1 * request.getParameterValues('script') >> ['onload="some function"']
            param == '"some function"'
            header == '"some function"'
            values[0] == '"some function"'
    }
}

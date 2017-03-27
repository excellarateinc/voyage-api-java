package voyage.security

import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class XssServletFilterSpec extends Specification {
    HttpServletRequest request
    HttpServletResponse response
    XSSRequestWrapper xssRequestWrapper

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        xssRequestWrapper = new XSSRequestWrapper(request)
    }

    def 'filter wraps the incoming request with a XSSRequestWrapper'() {
        given:
            XssServletFilter filter = new XssServletFilter()
            FilterChain filterChain = Mock(FilterChain)

        when:
            filter.doFilterInternal(request, response, filterChain)

        then:
            1 * filterChain.doFilter(*_) >> { args ->
                assert args[0] instanceof XSSRequestWrapper
            }
    }

    def 'filter returns decoded HTML to verify that the HTML input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('html')
            String header = xssRequestWrapper.getHeader('html')

        then:
            1 * request.getParameter('html') >> '<html><body><h1>Hello > see this &amp; </h1></body></html>'
            1 * request.getHeader('html') >> '<html><body><h1>Hello > see this &amp; </h1></body></html>'
            param == '<html><body><h1>Hello > see this & </h1></body></html>'
            header == '<html><body><h1>Hello > see this & </h1></body></html>'
    }

    def 'filter returns decoded CSS to verify that the CSS input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('css')
            String header = xssRequestWrapper.getHeader('css')

        then:
            1 * request.getParameter('css') >> '.test {\\26 \\" \\26}'
            1 * request.getHeader('css') >> '.test {\\26 \\" \\26}'
            param == '.test {\26 " \26}'
            header == '.test {\26 " \26}'
    }

    def 'filter returns decoded URI to verify that the URI input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('uri')
            String header = xssRequestWrapper.getHeader('uri')

        then:
            1 * request.getParameter('uri') >> '/test?param=%41%42%43%31%32%33'
            1 * request.getHeader('uri') >> '/test?param=%41%42%43%31%32%33'
            param == '/test?param=ABC123'
            header == '/test?param=ABC123'
    }

    def 'filter returns decoded Javascript to verify that the Javascript input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('js')
            String header = xssRequestWrapper.getHeader('js')

        then:
            1 * request.getParameter('js') >> 'if (\\\'1\\\' == \'1\' || \\"1\\" == "1")'
            1 * request.getHeader('js') >> 'if (\\\'1\\\' == \'1\' || \\"1\\" == "1")'
            param == 'if (\'1\' == \'1\' || "1" == "1")'
            header == 'if (\'1\' == \'1\' || "1" == "1")'
    }

    def 'filter returns decoded VBScript to verify that the VBScript input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('vbs')
            String header = xssRequestWrapper.getHeader('vbs')

        then:
            1 * request.getParameter('vbs') >> '\\" something here'
            1 * request.getHeader('vbs') >> '\\" something here'
            param == '" something here'
            header == '" something here'
    }

    def 'filter returns decoded XML to verify that the XML input is valid'() {
        when:
            String param = xssRequestWrapper.getParameter('xml')
            String header = xssRequestWrapper.getHeader('xml')

        then:
            1 * request.getParameter('xml') >> '<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>'
            1 * request.getHeader('xml') >> '<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>'
            param == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'
            header == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'
    }

    def 'filter does not remove normal text'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'This is normal text < with some & symbols that are acceptable.'
            1 * request.getHeader('script') >> 'This is normal text < with some & symbols that are acceptable.'
            param == 'This is normal text < with some & symbols that are acceptable.'
            header == 'This is normal text < with some & symbols that are acceptable.'
    }

    def 'filter removes suspicious scripting: <script>(.*?)</script>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> '<script>(.*?)</script>'
            1 * request.getHeader('script') >> '<script>(.*?)</script>'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: src[\\r\\n]*=[\\r\\n]*\'(.*?)\''() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'src=\'something\''
            1 * request.getHeader('script') >> 'src=\'something\''
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: src[\\r\\n]*=[\\r\\n]*"(.*?)"'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'src="something"'
            1 * request.getHeader('script') >> 'src="something"'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: </script>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> '</script>'
            1 * request.getHeader('script') >> '</script>'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: <script(.*?)>'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> '<script(.*?)>'
            1 * request.getHeader('script') >> '<script(.*?)>'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: eval\\((.*?)\\)'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'eval(something)'
            1 * request.getHeader('script') >> 'eval(something)'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: expression\\((.*?)\\)'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'expression(something)'
            1 * request.getHeader('script') >> 'expression(something)'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: javascript:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'javascript:'
            1 * request.getHeader('script') >> 'javascript:'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: vbscript:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'vbscript:'
            1 * request.getHeader('script') >> 'vbscript:'
            param == ''
            header == ''
    }

    def 'filter removes suspicious scripting: onload:'() {
        when:
            String param = xssRequestWrapper.getParameter('script')
            String header = xssRequestWrapper.getHeader('script')

        then:
            1 * request.getParameter('script') >> 'onload="some function"'
            1 * request.getHeader('script') >> 'onload="some function"'
            param == '"some function"'
            header == '"some function"'
    }
}

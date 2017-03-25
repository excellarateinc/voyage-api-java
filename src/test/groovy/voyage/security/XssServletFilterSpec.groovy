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

    def 'getParameter(..) returns decoded HTML to verify that the HTML input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('html')

        then:
            1 * request.getParameter('html') >> '<html><body><h1>Hello > see this &amp; </h1></body></html>'
            paramValue == '<html><body><h1>Hello > see this & </h1></body></html>'

    }

    def 'getParameter(..) returns decoded CSS to verify that the CSS input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('css')

        then:
            1 * request.getParameter('css') >> '.test {\\26 \\" \\26}'
            paramValue == '.test {\26 " \26}'
    }

    def 'getParameter(..) returns decoded URI to verify that the URI input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('uri')

        then:
            1 * request.getParameter('uri') >> '/test?param=%41%42%43%31%32%33'
            paramValue == '/test?param=ABC123'
    }

    def 'getParameter(..) returns decoded Javascript to verify that the Javascript input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('js')

        then:
            1 * request.getParameter('js') >> 'if (\\\'1\\\' == \'1\' || \\"1\\" == "1")'
            paramValue == 'if (\'1\' == \'1\' || "1" == "1")'

    }

    def 'getParameter(..) returns decoded VBScript to verify that the VBScript input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('vbs')

        then:
            1 * request.getParameter('vbs') >> '\\" something here'
            paramValue == '" something here'

    }

    def 'getParameter(..) returns decoded XML to verify that the XML input is valid'() {
        when:
            String paramValue = xssRequestWrapper.getParameter('xml')

        then:
            1 * request.getParameter('xml') >> '<xml><category><name>Something &amp; Another &#x3A3; &#198;</name></category</xml>'
            paramValue == '<xml><category><name>Something & Another Σ Æ</name></category</xml>'

    }
}

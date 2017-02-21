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

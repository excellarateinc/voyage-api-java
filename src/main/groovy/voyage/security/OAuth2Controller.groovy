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

    // TODO Document the OAuth2 endpoints, and include exception response codes like 401_unauthorized

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

package launchpad.security

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView

@Controller
@SessionAttributes("authorizationRequest")
class OAuth2Controller {

    @RequestMapping("/oauth/confirm_access")
    ModelAndView getAccessConfirmation(Map<String, Object> model) throws Exception {
        return new ModelAndView("oauth-confirm-access", model)
    }
}

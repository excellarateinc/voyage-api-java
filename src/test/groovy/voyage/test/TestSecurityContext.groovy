package voyage.test

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext

class TestSecurityContext implements SecurityContext {
    Authentication authentication = new TestAuthentication()
}

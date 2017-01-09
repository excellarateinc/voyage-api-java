package launchpad.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler

/**
 * Enables security at the app method level using @PreAuthorize annotations. This configuration extends the expression
 * handler to allow for OAuth2 expressions like "#oauth2.hasScope('Read_Data')".
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new OAuth2MethodSecurityExpressionHandler()
    }
}


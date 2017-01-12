package launchpad.security

import launchpad.error.UserVerificationRequiredException
import launchpad.security.user.User
import launchpad.security.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import java.security.Principal

/**
 * Servlet filter that inspects the currently authenticated user to see if they are required to verify their account
 * before gaining access to any resources. This filter is very single focused to only checking the User.isVerifyRequired
 * property to see if it is true. If the User.isVerifyRequired parameter is true, then a UserVerificationRequiredException
 * is thrown that will then be caught and translated by this app into a properly formatted JSON response to the consumer.
 *
 * Note: If an authenticated UserDetails object cannot be found for any reason, then this filter will simply pass control
 * over to the next filter in the filter chain. All responsibilities for security and proper authentication are left to
 * Spring Security to manage.
 */
@Component
class UserVerificationServletFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(UserVerificationServletFilter)
    private WebApplicationContext webAppContext
    private UserService userService
    private List resourcePathExclusions

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        webAppContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext())
        userService = webAppContext.getBean(UserService)
        resourcePathExclusions = webAppContext.environment.getProperty("security.user-verification.exclude-resources", List)
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request

        if (isRequestFilterable(httpRequest)) {
            isUserVerificationRequired(httpRequest)
        } else {
            LOG.debug("USER VERIFICATION FILTER: Request path ${getRequestPath(request)} is excluded from this filter. Skipping user verification.")
        }

        // Passed or skipped verification check. Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }

    @Override
    void destroy() {
        // Implemented to conform to the Filter interface. Nothing to do here.
    }

    private void isUserVerificationRequired(HttpServletRequest httpRequest) {
        Principal authenticatedUser = httpRequest.getUserPrincipal()

        if (authenticatedUser) {
            if (authenticatedUser instanceof Authentication) {
                String username

                Authentication authenticationToken = (Authentication)authenticatedUser
                if (authenticationToken.principal instanceof UserDetails) {
                    username = ((UserDetails)authenticationToken.principal).username

                } else if (authenticationToken.principal instanceof String) {
                    username = authenticationToken.principal
                }

                if (username) {
                    throwExceptionIfUserVerifyRequired(username)
                } else {
                    LOG.debug("USER VERIFICATION FILTER: Authenticated principal is not a recognized object. Skipping user verification.")
                }
            } else {
                LOG.debug("USER VERIFICATION FILTER: Authenticated user is not a recognized Authorization object. Skipping user verification.")
            }
        } else {
            LOG.debug("USER VERIFICATION FILTER: User is not authenticated. Skipping user verification.")
        }
    }

    private void throwExceptionIfUserVerifyRequired(String username) {
        User user = userService.findByUsername(username)
        if (user) {
            if (user.isVerifyRequired) {
                LOG.info("USER VERIFICATION FILTER: User requires verification. Returning error response.")
                throw new UserVerificationRequiredException()
            } else if (user) {
                LOG.debug("USER VERIFICATION FILTER: User does not require verification")
            }
        } else {
            LOG.debug("USER VERIFICATION FILTER: User was not found in the database. Skipping user verification.")
        }
    }

    private boolean isRequestFilterable(HttpServletRequest request) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()

        for (String antPattern : resourcePathExclusions) {
            if (antPathMatcher.match(antPattern, path)) {
                return false
            }
        }
        return true
    }

    private static String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath()
        if (request.pathInfo) {
            url += request?.pathInfo
        }
        return url
    }
}
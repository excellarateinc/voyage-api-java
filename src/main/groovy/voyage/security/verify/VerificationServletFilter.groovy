package voyage.security.verify

import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

/**
 * Servlet filter that inspects the currently authenticated user to see if they are required to verify their profile
 * before gaining access to any resources. This filter is very single focused to only checking the User.isVerifyRequired
 * property to see if it is true. If the User.isVerifyRequired parameter is true, then a UserVerificationRequiredException
 * is thrown that will then be caught and translated by this app into a properly formatted JSON response to the consumer.
 *
 * Note: If an authenticated UserDetails object cannot be found for any reason, then this filter will simply pass control
 * over to the next filter in the filter chain. All responsibilities for security and proper authentication are left to
 * Spring Security to manage.
 */
@Component
class VerificationServletFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(VerificationServletFilter)
    private final UserService userService

    @Value('${security.user-verification.exclude-resources}')
    private String[] resourcePathExclusions

    @Autowired
    VerificationServletFilter(UserService userService) {
        this.userService = userService
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        // Implemented to conform to the Filter interface. Nothing to do here.
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request
        HttpServletResponse httpResponse = (HttpServletResponse)response

        if (isRequestFilterable(httpRequest)) {
            if (isUserVerificationRequired(httpRequest)) {
                writeUserVerificationResponse(httpResponse)
                return
            }

        } else {
            LOG.debug("USER VERIFICATION FILTER: Request path ${getRequestPath(httpRequest)} is excluded from this filter. " +
                    'Skipping user verification.')
        }

        // Passed or skipped verification check. Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }

    @Override
    void destroy() {
        // Implemented to conform to the Filter interface. Nothing to do here.
    }

    private boolean isUserVerificationRequired(HttpServletRequest httpRequest) {
        Principal authenticatedUser = httpRequest.userPrincipal
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
                    User user = userService.findByUsername(username)
                    if (user) {
                        if (user.isVerifyRequired) {
                            LOG.info('USER VERIFICATION FILTER: User requires verification. Returning error response.')
                            return true
                        } else if (user) {
                            LOG.debug('USER VERIFICATION FILTER: User does not require verification')
                        }
                    } else {
                        LOG.debug('USER VERIFICATION FILTER: User was not found in the database. Skipping user verification.')
                    }
                } else {
                    LOG.debug('USER VERIFICATION FILTER: Authenticated principal is not a recognized object. Skipping user verification.')
                }
            } else {
                LOG.debug('USER VERIFICATION FILTER: Authenticated user is not a recognized Authorization object. Skipping user verification.')
            }
        } else {
            LOG.debug('USER VERIFICATION FILTER: User is not authenticated. Skipping user verification.')
        }
        return false
    }

    private static void writeUserVerificationResponse(HttpServletResponse response) {
        Map errorResponse = [
            error:'403_verify_user',
            errorDescription:'User verification is required',
        ]
        JsonBuilder json = new JsonBuilder([errorResponse])

        response.contentType = 'application/json'
        response.status = HttpStatus.FORBIDDEN.value()
        Writer responseWriter = response.writer
        json.writeTo(responseWriter)
        responseWriter.close()
        responseWriter.flush()
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
        String url = request.servletPath
        if (request.pathInfo) {
            url += request?.pathInfo
        }
        return url
    }
}

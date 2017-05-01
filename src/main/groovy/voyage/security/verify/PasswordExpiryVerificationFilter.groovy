package voyage.security.verify

import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import java.security.Principal

import voyage.security.user.User
import voyage.security.user.UserService

@Component
@Order(2)
class PasswordExpiryVerificationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordExpiryVerificationFilter)

    @Value('${security.password-verification.password-reset-days}')
    private int passwordResetDays

    @Value('${security.user-verification.exclude-resources}')
    private String[] userResourcePathExclusions

    @Value('${security.password-verification.exclude-resources}')
    private String[] passwordResourcePathExclusions

    private final UserService userService

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
    }

    @Autowired
    PasswordExpiryVerificationFilter(UserService userService) {
        this.userService = userService
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request
        if (isRequestFilterable(httpRequest)) {
            if (isUserCredentialsExpired(httpRequest)) {
                writePasswordVerificationResponse(response)
                return
            }
        } else {
            LOG.debug("PASSWORD EXPIRY VERIFICATION FILTER: Request path ${getRequestPath(httpRequest)} is excluded from " +
                    'this filter. Skipping password expiry verification.')
        }
        chain.doFilter(request, response)
    }

    private boolean isUserCredentialsExpired(HttpServletRequest httpRequest) {
        Principal authenticatedUser = httpRequest.userPrincipal
        if (!authenticatedUser) {
            LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: User is not authenticated. Skipping password expiry verification.')
            return false
        }
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
                    if (user.isCredentialsExpired || isPasswordExpired(user)) {
                        LOG.info('PASSWORD EXPIRY VERIFICATION FILTER: User password expired. Returning error response.')
                        return true
                    }
                } else {
                    LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: User was not found in the database. Skipping password expiry verification.')
                }
            } else {
                LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: Authenticated principal is not a recognized object. ' +
                        'Skipping password expiry verification.')
            }
        } else {
            LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: Authenticated user is not a recognized Authorization object.' +
                    ' Skipping password expiry verification.')
        }
        return false
    }

    private static void writePasswordVerificationResponse(ServletResponse response) {
        Map errorResponse = [
                error:'403_password_expired',
                errorDescription:'Password is expired. Please change the password to access the application',
        ]
        JsonBuilder json = new JsonBuilder([errorResponse])
        response.contentType = 'application/json'
        Writer responseWriter = response.writer
        json.writeTo(responseWriter)
        responseWriter.close()
        responseWriter.flush()
    }

    private boolean isRequestFilterable(HttpServletRequest request) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        for (String antPattern : userResourcePathExclusions) {
            if (antPathMatcher.match(antPattern, path)) {
                return false
            }
        }
        for (String antPattern : passwordResourcePathExclusions) {
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

    private boolean isPasswordExpired(User user) {
        Integer diffInDays = new Date() - user.passwordCreatedDate
        if (passwordResetDays == 0) {
            return false
        }
        return  diffInDays > passwordResetDays
    }

    @Override
    void destroy() {

    }
}

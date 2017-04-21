package voyage.security.verify

import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
import java.security.Principal

/**
 * Created by user on 4/20/2017.
 */
@Component
class PasswordExpiryVerificationFilter implements Filter{
    private static final Logger LOG = LoggerFactory.getLogger(PasswordExpiryVerificationFilter)
    private final UserService userService
    @Value('${security.password-verification.password-reset-days}')
    private int passwordResetDays
    @Value('${security.password-verification.exclude-resources}')
    private String[] resourcePathExclusions
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
                writeUserVerificationResponse(response)
                return
            }
        } else {
            println("USER VERIFICATION FILTER: Request path ${getRequestPath(httpRequest)} is excluded from this filter. " +
                    'Skipping user verification.')
        LOG.debug("USER VERIFICATION FILTER: Request path ${getRequestPath(httpRequest)} is excluded from this filter. " +
                'Skipping user verification.')
        }

        // Passed or skipped verification check. Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }
    private boolean isUserCredentialsExpired(HttpServletRequest httpRequest) {
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

                        if (user.isCredentialsExpired || isPasswordExpired(user)) {
                            LOG.info('USER PASSWORD VERIFICATION FILTER: User password expired. Returning error response.')
                            return true
                        }
                    } else {
                        LOG.debug('USER PASSWORD VERIFICATION FILTER: User was not found in the database. Skipping user verification.')
                    }
                } else {
                    LOG.debug('USER PASSWORD VERIFICATION FILTER: Authenticated principal is not a recognized object. Skipping user verification.')
                }
            } else {
                LOG.debug('USER PASSWORD VERIFICATION FILTER: Authenticated user is not a recognized Authorization object. Skipping user verification.')
            }
        } else {
            LOG.debug('USER PASSWORD VERIFICATION FILTER: User is not authenticated. Skipping user verification.')
        }
        return false
    }
    private static void writeUserVerificationResponse(ServletResponse response) {
        Map errorResponse = [
                error:'403_password_expired',
                errorDescription:'User password change is required',
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

        for (String antPattern : resourcePathExclusions) {
            println("1."+antPattern)
            println("2."+path)
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
    private boolean isPasswordExpired(User user){
        return  passwordResetDays!= 0 | ((new Date() - user.passwordCreatedDate  ) > passwordResetDays ) ? true :false
    }
    @Override
    void destroy() {

    }
}

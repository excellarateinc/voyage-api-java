package voyage.security.verify

import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class PasswordExpiryVerificationFilterSpec extends Specification {
    PasswordExpiryVerificationFilter filter
    UserService userService
    String[] resourcePathExclusions
    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain

    void setup() {
        userService = Mock(UserService)
        resourcePathExclusions = ['/test/**', '/test2']
        filter = new PasswordExpiryVerificationFilter(userService)
        filter.userResourcePathExclusions = resourcePathExclusions
        filter.passwordResourcePathExclusions = resourcePathExclusions
        filter.passwordResetDays = 90

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
    }

    def 'Request (without path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.servletPath >> '/test/something'
            0 * request.userPrincipal
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request (with path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.servletPath >> '/test/something?paramA=1&paramB=2'
            0 * request.userPrincipal
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable but the user is not authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> null
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, the principal is authenticated, but not an instance of Authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> Mock(Principal)
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has Authentication user token, but credentials are expired'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:true, passwordCreatedDate:new Date() )
            1 * request.pathInfo
            1 * response.writer >> responseWriter
            1 * responseWriter.append('[{"error":"403_password_expired",' +
                    '"errorDescription":"Password is expired. Please change the password to access the application"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has Authentication user token, but credentials are not expired'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() )
            1 * request.pathInfo
            1 * filterChain.doFilter(request, response)
    }

    def 'Logged in user credentials are not expired but date of password creation is more than expiration days'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() - 100 )
            1 * request.pathInfo
            1 * response.writer >> responseWriter
            1 * responseWriter.append('[{"error":"403_password_expired",' +
                    '"errorDescription":"Password is expired. Please change the password to access the application"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }

    def 'Logged in user credentials are not expired and date of password creation is more than expiration days but passwordResetDays is 0'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            filter.passwordResetDays = 0

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() - 100 )
            1 * request.pathInfo
            1 * filterChain.doFilter(request, response)
    }

}

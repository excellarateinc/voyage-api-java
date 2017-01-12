package launchpad.security

import launchpad.security.user.User
import launchpad.security.user.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class UserVerificationServletFilterSpec extends Specification {
    UserVerificationServletFilter filter
    UserService userService
    String[] resourcePathExclusions
    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain

    void setup() {
        userService = Mock(UserService)
        resourcePathExclusions = ['/test/**', '/test2']
        filter = new UserVerificationServletFilter(userService, resourcePathExclusions)

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
    }

    def 'Request (without path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.getServletPath() >> "/test/something"
            0 * request.getUserPrincipal()
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request (with path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.getServletPath() >> "/test/something?paramA=1&paramB=2"
            0 * request.getUserPrincipal()
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable but the user is not authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.getServletPath() >> "/filterable/request"
            1 * request.getUserPrincipal() >> null
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, the principal is authenticated, but not an instance of Authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.getServletPath() >> "/filterable/request"
            1 * request.getUserPrincipal() >> Mock(Principal)
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has Authentication user token, and does NOT require verification'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.getServletPath() >> "/filterable/request"
            1 * request.getUserPrincipal() >> userPrincipal
            2 * userPrincipal.getPrincipal() >> userDetails
            1 * userDetails.username >> "test"
            1 * userService.findByUsername("test") >> new User()
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has UserDetails auth token, and DOES require verification'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
           filter.doFilter(request, response, filterChain)

        then:
            1 * request.getServletPath() >> "/filterable/request"
            1 * request.getUserPrincipal() >> userPrincipal
            2 * userPrincipal.getPrincipal() >> userDetails
            1 * userDetails.username >> "test"
            1 * userService.findByUsername("test") >> new User(isVerifyRequired: true)
            1 * response.getWriter() >> responseWriter
            1 * responseWriter.append('[{"error":"403_verify_user","errorDescription":"User verification is required"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has String auth token, and DOES require verification'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.getServletPath() >> "/filterable/request"
            1 * request.getUserPrincipal() >> userPrincipal
            3 * userPrincipal.getPrincipal() >> "test"
            1 * userService.findByUsername("test") >> new User(isVerifyRequired: true)
            1 * response.getWriter() >> responseWriter
            1 * responseWriter.append('[{"error":"403_verify_user","errorDescription":"User verification is required"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }
}

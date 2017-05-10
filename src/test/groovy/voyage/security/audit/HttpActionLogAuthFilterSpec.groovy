package voyage.security.audit

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class HttpActionLogAuthFilterSpec extends Specification {
    def 'doFilterInternal sets attributes for user and client'() {
        given:
            User user = new User()
            Client client = new Client()

            UserService userService = Mock(UserService)
            ClientService clientService = Mock(ClientService)

            HttpServletRequest request = Mock(HttpServletRequest)
            HttpServletResponse response = Mock(HttpServletResponse)
            FilterChain filterChain = Mock(FilterChain)

            HttpActionLogAuthFilter filter = new HttpActionLogAuthFilter(userService, clientService)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * userService.currentUser >> user
            1 * clientService.currentClient >> client
            1 * request.userPrincipal >> {
                Principal principal = Mock(Principal)
                principal.name >> 'test-user'
                return principal
            }

            1 * request.setAttribute(HttpActionLogFilter.USER_PRINCIPAL_KEY, 'test-user')
            1 * request.setAttribute(HttpActionLogFilter.USER_KEY, user)
            1 * request.setAttribute(HttpActionLogFilter.CLIENT_KEY, client)

            1 * filterChain.doFilter(request, response)
    }
}

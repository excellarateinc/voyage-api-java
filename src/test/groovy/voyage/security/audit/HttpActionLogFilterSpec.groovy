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

class HttpActionLogFilterSpec extends Specification {

    // with proxy IP

    def 'save a request action log happy path'() {
        given:
            def actionLogService = Mock(ActionLogService)
            def userService = Mock(UserService)
            def clientService = Mock(ClientService)

            def request = Mock(HttpServletRequest)
            def response = Mock(HttpServletResponse)
            def filterChain = Mock(FilterChain)

            User currentUser = new User()
            Client currentClient = new Client()

            HttpActionLogFilter filter = new HttpActionLogFilter(actionLogService, userService, clientService)

        when:
            filter.doFilterInternal(request, response, filterChain)

        then:
            1 * request.remoteAddr >> '127.0.0.1'
            1 * request.protocol >> 'http'
            1 * request.method >> 'GET'
            1 * request.authType >> 'test'
            2 * request.userPrincipal >> Mock(Principal)
            1 * request.userPrincipal.name >> 'test-user'
            1 * userService.currentUser >> currentUser
            1 * clientService.currentClient >> currentClient
            1 * request.requestURL >> new StringBuffer('http://test.request.url')
            2 * request.queryString >> 'param1=valueA'

            1 * response.status >> 200

            2 * actionLogService.saveDetached(_ as ActionLog) >> new ActionLog()
    }
}
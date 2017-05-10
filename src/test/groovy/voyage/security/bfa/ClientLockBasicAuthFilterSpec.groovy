package voyage.security.bfa

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class ClientLockBasicAuthFilterSpec extends Specification {
    ClientLockBasicAuthFilter filter
    ClientService clientService

    HttpServletRequest request
    HttpServletResponse response
    HttpSession session
    FilterChain filterChain

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
        session = Mock(HttpSession)

        clientService = Mock(ClientService)

        filter = new ClientLockBasicAuthFilter(clientService)
        filter.isEnabled = true
        filter.resourcePaths = '/**'
        filter.maxLoginAttempts = 5
    }

    def 'doFilterInternal is skipped if disabled'() {
        given:
            filter.isEnabled = false

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal skips request if the url doesn not match'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/nomatch']

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal finds a username and the user is authenticated'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/**']

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> true
            1 * filterChain.doFilter(request, response)

            0 * clientService.findByClientIdentifier('super')
            0 * clientService.save(_ as Client)
    }

    def 'doFilterInternal client failed authentication and increments failure attempts'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/**']

            Client client = new Client()
            client.isEnabled = true
            client.isAccountLocked = false

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> false
            1 * filterChain.doFilter(request, response)

            1 * clientService.findByClientIdentifier('client-super') >> client
            1 * clientService.save(_ as Client)

            client.failedLoginAttempts == 1
    }

    def 'doFilterInternal client failed authentication and locks the client account'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/**']

            Client client = new Client()
            client.isEnabled = true
            client.isAccountLocked = false
            client.failedLoginAttempts = 4

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> false
            1 * filterChain.doFilter(request, response)

            1 * clientService.findByClientIdentifier('client-super') >> client
            1 * clientService.save(_ as Client)

            client.failedLoginAttempts == 5
            client.isAccountLocked
    }
}

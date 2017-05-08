package voyage.security.audit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import voyage.security.client.ClientService
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter that captures the authenticated user information and stores them into the request attributes for the
 * HttpActionLogFilter. The HttpActionLogFilter is ordered at the beginning of the filter chain and before
 * the Spring Security filters, which means that the request and session are cleared of authentication information before
 * the response gets back to the HttpActionLogFilter. The HttpActionLogFilter needs the authentication information, so
 * this filter provides the information necessary to log the authenticated users. 
 */
@Component
class HttpActionLogAuthFilter extends OncePerRequestFilter {
    private final UserService userService
    private final ClientService clientService

    @Autowired
    HttpActionLogAuthFilter(UserService userService, ClientService clientService) {
        this.userService = userService
        this.clientService = clientService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        request.setAttribute(HttpActionLogFilter.USER_PRINCIPAL_KEY, request.userPrincipal?.name)
        request.setAttribute(HttpActionLogFilter.USER_KEY, userService.currentUser)
        request.setAttribute(HttpActionLogFilter.CLIENT_KEY, clientService.currentClient)

        filterChain.doFilter(request, response)
    }
}

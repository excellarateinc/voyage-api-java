package voyage.security.bfa

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import voyage.security.client.Client
import voyage.security.client.ClientService

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This filter exists because the Spring Security OAuth2 usage of the BasicAuthenticationFilter doesn't setup the
 * AuthenticationManager to trigger an AuthenticationEvent after a successful for failed login attempt. In order to
 * detect if an OAuth2 Client basic auth attempt succeeded for failed, a servlet filter must be present before the
 * invocation of the BasicAuthenticationFilter request to catch when the filter fails and a servlet filter must be
 * present after the invocation of BasicAuthenticationFilter request to catch when the filter succeeds.
 *
 * ClientLockBasicAuthFilter is positioned BEFORE the BasicAuthenticationFilter filter to catch failed basic auth
 * requests. This class is only focused on OAuth2 Clients and should only listen in on the /oauth/ request path. Spring
 * Security configures a different BasicAuthenticationFilter on all other request paths that is configured properly and
 * will trigger an AuthenticationEvent upon a success or failed Basic Auth request.
 *
 * NOTE: ClientLockBasicAuthFilter will set an HttpSession attribute on a successful login that will be used by this
 * class to determine if a successful login occurred. By the time the response is examined by this class, the Authorization
 * object is null within the Spring SecurityContext.
 */
@Component
@Order(-10000)
class ClientLockBasicAuthFilter extends BasicAuthFilter {
    private final ClientService clientService

    @Value('${security.brute-force-attack.client-lock-basic-auth-filter.enabled}')
    private boolean isEnabled

    @Value('${security.brute-force-attack.client-lock-basic-auth-filter.resources}')
    private String[] resourcePaths

    @Value('${security.brute-force-attack.client-lock-basic-auth-filter.max-login-attempts}')
    private int maxLoginAttempts

    ClientLockBasicAuthFilter(ClientService clientService) {
        this.clientService = clientService
        this.log = LoggerFactory.getLogger(ClientLockBasicAuthFilter)
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean isFilterable = false

        if (isEnabled) {
            isFilterable = isRequestFilterable(request, resourcePaths)
            if (isFilterable) {
                // Initialize the session so that isClientBasicAuthFailure() doesn't fail when checking the session for an
                // attribute when the Basic Auth request fails and doesn't create a session.
                request.getSession(true).setAttribute(IS_AUTHENTICATED, false)
            }
        } else {
            log.debug('ClientLockBasicAuthFilter is DISABLED. Skipping.')
        }

        filterChain.doFilter(request, response)

        if (isFilterable) {
            String username = findUsername(request)
            boolean isAuthenticated = request.session.getAttribute(IS_AUTHENTICATED)
            if (username && !isAuthenticated) {
                incrementFailedLoginAttempts(username)
            } else if (!username) {
                log.debug('No username parameters were found. Skipping.')
            } else if (isAuthenticated) {
                log.debug('User is authenticated. Skipping.')
            }
        }
    }

    private void incrementFailedLoginAttempts(String username) {
        Client client = clientService.findByClientIdentifier(username)
        if (client && client.isEnabled && !client.isAccountLocked) {
            if (log.debugEnabled) {
                log.debug('Found Client record in the database for username: ' + username)
            }

            if (!client.failedLoginAttempts) {
                client.failedLoginAttempts = 0
            }

            client.failedLoginAttempts = client.failedLoginAttempts + 1
            if (log.debugEnabled) {
                log.debug("Client ${username} has ${client.failedLoginAttempts} failed login attempts.")
            }

            if (client.failedLoginAttempts >= maxLoginAttempts) {
                if (log.debugEnabled) {
                    log.debug("Client ${username} has hit their max failed login attempts of ${maxLoginAttempts}. " +
                            "Locking Client with ID=${client.id}.")
                }
                client.isAccountLocked = true
            }

            clientService.save(client)

        } else if (log.debugEnabled) {
            if (!client) {
                log.debug('No Client record found in the database for username: ' + username)
            } else if (!client.isEnabled) {
                log.debug('The Client record is disabled for username: ' + username)
            } else if (client.isAccountLocked) {
                log.debug('The Client record is locked for username: ' + username)
            }
        }
    }
}

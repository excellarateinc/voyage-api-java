package voyage.security.bfa

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
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
 * ClientResetBasicAuthFilter is positioned AFTER the BasicAuthenticationFilter filter to catch successful basic auth
 * requests. This class is only focused on OAuth2 Clients and should only listen in on the /oauth/ request path. Spring
 * Security configures a different BasicAuthenticationFilter on all other request paths that is configured properly and
 * will trigger an AuthenticationEvent upon a success or failed Basic Auth request.
 *
 * NOTE: ClientResetBasicAuthFilter will set an HttpSession attribute on a successful login that will be used to notify
 * the ClientLockBasicAuthFilter that a successful login occurred. The ClientLockBasicAuthFilter doesn't have another
 * way to detect if the login was successful.
 */
@Component
class ClientResetBasicAuthFilter extends BasicAuthFilter {
    private ClientService clientService

    @Value('${security.brute-force-attack.client-lock-basic-auth-filter.enabled}')
    private boolean isEnabled

    @Value('${security.brute-force-attack.client-lock-basic-auth-filter.resources}')
    private String[] resourcePaths = ['/**']
    
    ClientResetBasicAuthFilter(ClientService clientService) {
        this.clientService = clientService
        this.LOG = LoggerFactory.getLogger(ClientResetBasicAuthFilter)
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isEnabled) {
            if (isRequestFilterable(request, resourcePaths)) {
                String username = findUsername(request)
                Authentication authentication = SecurityContextHolder.context.authentication
                if (username && authentication?.isAuthenticated()) {
                    resetFailedLoginAttempts(authentication, username)
                    request.session.setAttribute(IS_AUTHENTICATED, true)
                } else if (!username) {
                    LOG.debug('No username parameters were found. Skipping.')
                } else if (!authentication.isAuthenticated()) {
                    LOG.debug('User is not authenticated. Skipping.')
                }
            }
        } else {
            LOG.debug('ClientResetBasicAuthFilter is DISABLED. Skipping.')
        }

        filterChain.doFilter(request, response)
    }

    private void resetFailedLoginAttempts(Authentication authentication, String username) {
        if (authentication.principal instanceof User) {
            String authenticatedUsername = ((User)authentication.principal).username
            if (username == authenticatedUsername) {
                Client client = clientService.findByClientIdentifier(username)
                if (client) {
                    if (client.failedLoginAttempts > 0) {
                        if (LOG.debugEnabled) {
                            LOG.debug("Resetting client ${client.clientIdentifier} failed login attempts to 0.")
                        }
                        client.failedLoginAttempts = 0
                        clientService.save(client)
                    } else if (LOG.debugEnabled) {
                        LOG.debug("Client ${client.clientIdentifier} has no failed login attempts. Skipping update.")
                    }
                } else if (LOG.debugEnabled) {
                    LOG.debug("Could not find Client record for username ${username}. Skipping")
                }
            } else if (LOG.debugEnabled) {
                LOG.debug("Basic auth username (${username} didn't match Authentication username (${authenticatedUsername}). Skipping.")
            }
        }
    }
}

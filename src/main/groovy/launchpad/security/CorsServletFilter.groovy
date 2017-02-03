package launchpad.security

import launchpad.security.client.Client
import launchpad.security.client.ClientOrigin
import launchpad.security.client.ClientService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet filter that uses the currently authenticated user (if any) to determine which Origin's are allowed. The base
 * Spring Security CORS filter is very limited in that it accepts one Origin or all origins. When all origins are allowed
 * and "credentials" are allowed, then Spring Security echo's back the origin given to it. Echoing back request data
 * creates an opportunity for injection attacks, which is ultimately why this CorsServletFilter was created.
 *
 * Features
 * - if the 'client' requesting access to the API is authenticated
 *   - the given Origin is matched to the Client Origins in the database
 *   - if a match is found, then return the value in the database as the allowed origin
 *   - if no match is found, then default to being permissive and return a public wildcard allows for Origin
 * - if the request is anonymous
 *   - default to being permissive and return a public wildcard allows for Origin
 *
 * NOTE: Defaulting to permissive origin in this class because an assumption is made that the security framework will
 *       catch unauthorized requests and prevent access. For a more restrictive implementation, consider extending this
 *       class or replacing it with a different implementation.
 */
@Component
class CorsServletFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CorsServletFilter)
    private static final String HEADER_ORIGIN = 'Origin'
    private static final String HEADER_VARY = 'Vary'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = 'Access-Control-Allow-Origin'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = 'Access-Control-Allow-Credentials'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE = 'true'
    private static final String HEADER_CORS_WILDCARD_VALUE = '*'

    private final ClientService clientService

    @Autowired
    CorsServletFilter(ClientService clientService) {
        this.clientService = clientService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (isRequestFilterable(request, response)) {
            applyOriginResponseHeaders(request, response)

        } else {
            LOG.debug('CORS FILTER: Skipping CORS filtering for this request')
        }

        // Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }

    private void applyOriginResponseHeaders(HttpServletRequest request, HttpServletResponse response) {
        Client client = clientService.loggedInClient
        if (client && client.clientOrigins) {
            String requestOrigin = request.getHeader(HEADER_ORIGIN)
            ClientOrigin clientOriginMatch = client.clientOrigins.find { clientOrigin ->
                cleanUri(clientOrigin.originUri) == cleanUri(requestOrigin)
            }
            if (clientOriginMatch) {
                writeRestrictedResponseHeaders(response, clientOriginMatch.originUri)
                return
            }
        }
        writePublicResponseHeaders(response)
    }

    private static void writeRestrictedResponseHeaders(HttpServletResponse response, String origin) {
        response.addHeader(HEADER_VARY, HEADER_ORIGIN)
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, origin)
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS, HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE)
    }

    private static void writePublicResponseHeaders(HttpServletResponse response) {
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, HEADER_CORS_WILDCARD_VALUE)
    }

    private static boolean isRequestFilterable(HttpServletRequest request, HttpServletResponse response) {
        String originRequestHeader = request.getHeader(HEADER_ORIGIN)
        if (originRequestHeader) {
            if (!response.getHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN)) {
                 return true
            }
        }
        return false
    }

    private static String cleanUri(String uriIn) {
        String uri = uriIn.trim()
        if (uri.endsWith('/')) {
            int oneLessThanMax = uri.size() - 1
            uri = uri[0..oneLessThanMax]
        }
        return uri
    }
}

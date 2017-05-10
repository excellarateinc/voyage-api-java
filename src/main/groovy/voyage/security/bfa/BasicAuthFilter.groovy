package voyage.security.bfa

import org.slf4j.Logger
import org.springframework.security.crypto.codec.Base64
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Base filter class for filters that are intercepting basic auth requests.
 */
abstract class BasicAuthFilter extends OncePerRequestFilter {
    protected static final String IS_AUTHENTICATED = 'BASIC_AUTH.IS_AUTHENTICATED'
    protected static final String CHARSET = 'UTF-8'
    protected Logger log

    abstract protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException

    protected String findUsername(HttpServletRequest request) {
        log.debug('Looking for a username in Basic Auth header')
        String username = null
        String header = request.getHeader('Authorization')
        if (header?.startsWith('Basic ')) {
            if (log.debugEnabled) {
                log.debug('Found Basic Auth header: ' + header)
            }
            byte[] base64Token = header[6..-1].getBytes(CHARSET)
            byte[] decoded
            try {
                decoded = Base64.decode(base64Token)
            } catch (IllegalArgumentException ignore) {
                if (log.debugEnabled) {
                    log.debug('Could not decode the Basic Auth header value: ' + header)
                }
            }
            if (decoded) {
                String token = new String(decoded, CHARSET)
                if (log.debugEnabled) {
                    log.debug('Decoded Basic Auth token: ' + token)
                }
                int delimiter = token.indexOf(':')
                if (delimiter > -1) {
                    username = token[0..delimiter - 1]
                    if (log.debugEnabled) {
                        log.debug('Found username: ' + username)
                    }
                } else {
                    if (log.debugEnabled) {
                        log.debug('Could not extract username from token: ' + token)
                    }
                }
            }
        } else {
            if (log.debugEnabled) {
                log.debug('Authorization header is not Basic Auth. Skipping.')
            }
        }
        return username
    }

    protected boolean isRequestFilterable(HttpServletRequest request, String[] resourcePaths) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        for (String antPattern : resourcePaths) {
            if (antPathMatcher.match(antPattern, path)) {
                if (log.debugEnabled) {
                    log.debug("Request path ${path} matches this filter")
                }
                return true
            }
        }
        if (log.debugEnabled) {
            log.debug("Request path ${path} is excluded from this filter. Skipping.")
        }
        return false
    }

    protected static String getRequestPath(HttpServletRequest request) {
        String url = request.servletPath
        if (request.pathInfo) {
            url += request?.pathInfo
        }
        return url
    }
}

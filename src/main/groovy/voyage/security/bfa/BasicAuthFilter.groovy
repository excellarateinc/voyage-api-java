package voyage.security.bfa

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.codec.Base64
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.http.HttpServletRequest

/**
 * Base filter class for filters that are intercepting basic auth requests.
 */
abstract class BasicAuthFilter extends OncePerRequestFilter {
    protected static final String IS_AUTHENTICATED = 'BASIC_AUTH.IS_AUTHENTICATED'
    protected final String CHARSET = 'UTF-8'
    protected Logger LOG

    BasicAuthFilter() {
        LOG = LoggerFactory.getLogger(BasicAuthFilter)
    }

    protected String findUsername(HttpServletRequest request) {
        LOG.debug('Looking for a username in Basic Auth header')
        String username = null
        String header = request.getHeader("Authorization")
        if (header?.startsWith("Basic ")) {
            if (LOG.debugEnabled) {
                LOG.debug('Found Basic Auth header: ' + header)
            }
            byte[] base64Token = header.substring(6).getBytes(CHARSET)
            byte[] decoded
            try {
                decoded = Base64.decode(base64Token)
            } catch (IllegalArgumentException ignore) {
                if (LOG.debugEnabled) {
                    LOG.debug('Could not decode the Basic Auth header value: ' + header)
                }
                return null
            }
            String token = new String(decoded, CHARSET)
            if (LOG.debugEnabled) {
                LOG.debug('Decoded Basic Auth token: ' + token)
            }
            int delimiter = token.indexOf(':')
            if (delimiter > -1) {
                username = token.substring(0, delimiter)
                if (LOG.debugEnabled) {
                    LOG.debug('Found username: ' + username)
                }
            } else {
                if (LOG.debugEnabled) {
                    LOG.debug('Could not extract username from token: ' + token)
                }
            }
        } else {
            if (LOG.debugEnabled) {
                LOG.debug('Authorization header is not Basic Auth. Skipping.')
            }
        }
        return username
    }

    protected boolean isRequestFilterable(HttpServletRequest request, resourcePaths) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        for (String antPattern : resourcePaths) {
            if (antPathMatcher.match(antPattern, path)) {
                if (LOG.debugEnabled) {
                    LOG.debug("Request path ${path} matches this filter")
                }
                return true
            }
        }
        if (LOG.debugEnabled) {
            LOG.debug("Request path ${path} is excluded from this filter. Skipping.")
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

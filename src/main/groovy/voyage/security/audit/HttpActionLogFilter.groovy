package voyage.security.audit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import voyage.security.client.ClientService
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class HttpActionLogFilter extends OncePerRequestFilter {
    private static final String UNKNOWN = 'unknown'
    private static final int MAX_PAYLOAD_LENGTH = 10000
    private final ActionLogService actionLogService
    private final UserService userService
    private final ClientService clientService

    @Autowired
    HttpActionLogFilter(ActionLogService actionLogService, UserService userService, ClientService clientService) {
        this.actionLogService = actionLogService
        this.userService = userService
        this.clientService = clientService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis()
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request)
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response)

        // Save the request
        ActionLog actionLog = saveRequest(wrappedRequest)

        // Complete the request
        filterChain.doFilter(wrappedRequest, wrappedResponse)

        // Save the response
        saveResponse(wrappedRequest, wrappedResponse, actionLog, startTime)

        // Copy content of response back into original response
        wrappedResponse.copyBodyToResponse()
    }

    private ActionLog saveRequest(ContentCachingRequestWrapper request) {
        ActionLog actionLog = new ActionLog()
        actionLog.with {
            clientIpAddress = getClientIpAddress(request)
            clientProtocol = getClientProtocol(request)
            httpMethod = request.method
            authType = request.authType
            principal = request.userPrincipal?.name
            user = userService.currentUser
            client = clientService.currentClient
            url = request.requestURL
            if (request.queryString) {
                url + '?' + request.queryString
            }
            requestHeaders = getHeaders(request)
        }
        return actionLogService.saveDetached(actionLog)
    }

    private ActionLog saveResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                   ActionLog actionLog, Long startTime) {
        actionLog.durationMs = System.currentTimeMillis() - startTime
        actionLog.httpStatus = response.status
        actionLog.requestBody = getContentAsString(request.contentAsByteArray, request.characterEncoding)
        actionLog.responseBody = getContentAsString(response.contentAsByteArray, response.characterEncoding)
        actionLogService.saveDetached(actionLog)
    }

    private static String getContentAsString(byte[] buf, String charsetName) {
        if (buf == null || buf.length == 0) {
            return ''
        }
        int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH)
        try {
            return new String(buf, 0, length, charsetName)
        } catch (UnsupportedEncodingException ignore) {
            return 'Unsupported Encoding'
        }
    }

    private static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader('X-Forwarded-For')
        if (!ip || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.remoteAddr
        }
        return ip
    }

    private static String getClientProtocol(HttpServletRequest request) {
        String ip = request.getHeader('X-Forwarded-Proto')
        if (!ip || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.protocol
        }
        return ip
    }

    private static String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder()
        request.headerNames.each { name ->
            headers.append(name).append(':').append(request.getHeader(name)).append(', ')
        }
        if (headers) {
            int maxLength = headers.length() - 3
            return headers[0..maxLength]
        }
        return headers.toString()
    }
}

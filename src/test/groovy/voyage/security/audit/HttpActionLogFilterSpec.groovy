package voyage.security.audit

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

class HttpActionLogFilterSpec extends Specification {
    HttpActionLogFilter filter
    ActionLogService actionLogService
    UserService userService
    ClientService clientService
    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain
    User currentUser
    Client currentClient

    def setup() {
        actionLogService = Mock(ActionLogService)
        userService = Mock(UserService)
        clientService = Mock(ClientService)
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)

        currentUser = Mock(User)
        currentClient = Mock(Client)

        filter = new HttpActionLogFilter(actionLogService, userService, clientService)
    }

    def 'filterRequestBody with FORM content type with invalid content'() {
        given:
            String body = 'This is not typical form information!'
        when:
            String filteredBody = filter.filterRequestBody('application/x-www-form-urlencoded', body)
        then:
           body == filteredBody
    }

    def 'filterRequestBody with FORM content type and no masked fields'() {
        given:
            String body = 'test=value2&test2&test3=value3'
        when:
            String filteredBody = filter.filterRequestBody('application/x-www-form-urlencoded', body)
        then:
            body == filteredBody
    }

    def 'filterRequestBody with FORM content type property masks fields'() {
        given:
           String body = 'password=value2&test2=value2&test3=value3'
        when:
            String filteredBody = filter.filterRequestBody('application/x-www-form-urlencoded', body)
        then:
            'password=*********&test2=value2&test3=value3' == filteredBody
    }

    def 'filterRequestBody with JSON content type and invalid content'() {
        given:
            String body = '} This is not { JSON'
        when:
            String filteredBody = filter.filterRequestBody('application/json', body)
        then:
           body == filteredBody
    }

    def 'filterRequestBody with JSON content type and no masked fields'() {
        given:
            String body = '{"test1":{"sub1":"sub-value1"},"test2":"value2","test3":"value3"}'
        when:
            String filteredBody = filter.filterRequestBody('application/json', body)
        then:
            body == filteredBody
    }

    def 'filterRequestBody with JSON content type and masked fields'() {
        given:
            String body = '{"test1":{"password":"sub-value1"},"test2":"value2","test3":"value3"}'
        when:
            String filteredBody = filter.filterRequestBody('application/json', body)
        then:
            '{"test1":{"password":"*********"},"test2":"value2","test3":"value3"}' == filteredBody
    }

    def 'filterRequestBody with UNKNOWN content type returns content unchanged'() {
        given:
            String body = 'This is some random content'
        when:
            String filteredBody = filter.filterRequestBody('UNKNOWN', body)
        then:
            body == filteredBody
    }

    def 'lookupBasicAuthUsername with no Authorization header'() {
        when:
            String username = filter.lookupBasicAuthUsername(request)
        then:
            request.getHeader('Authorization') >> null
            !username
    }

    def 'lookupBasicAuthUsername with Authorization header that is not Basic'() {
        when:
            String username = filter.lookupBasicAuthUsername(request)
        then:
            request.getHeader('Authorization') >> "SOMETHING"
            !username
    }

    def 'lookupBasicAuthUsername with Authorization header that is a valid Basic value'() {
        when:
            String username = filter.lookupBasicAuthUsername(request)
        then:
            request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            'client-super' == username
    }

    def 'getRequestPath with no query params'() {
        when:
            String path = filter.getRequestPath(request)
        then:
            request.servletPath >> 'http://something/api'
            'http://something/api' == path
    }

    def 'getRequestPath with query params'() {
        when:
            String path = filter.getRequestPath(request)
        then:
            request.servletPath >> 'http://something/api'
            request.pathInfo >> '?key=value&key2=value2'
            'http://something/api?key=value&key2=value2' == path
    }

    def 'isRequestFilterable defaults to match any path'() {
        when:
            boolean isFilterable = filter.isRequestFilterable(request)
        then:
            request.servletPath >> '/api'
            isFilterable
    }

    def 'isRequestFilterable matches only specific paths'() {
        given:
            filter.excludeResourcePaths = ['/nomatch']
        when:
            boolean isFilterable = filter.isRequestFilterable(request)
        then:
            request.servletPath >> '/api'
            isFilterable
    }

    def 'isRequestFilterable matches /api'() {
        given:
           filter.excludeResourcePaths = ['/resources/**']
        when:
            boolean isFilterable = filter.isRequestFilterable(request)
        then:
            request.servletPath >> '/resources/something.jpg'
            !isFilterable
    }

    def 'appendMasked applies values that do not need to be masked'() {
        given:
            StringBuilder builder = new StringBuilder()
            String key = 'KEY1'
            String value = 'VALUE1'
            String delimiter = ':'
        when:
            StringBuilder responseBuilder = filter.appendMasked(builder, key, value, delimiter)
        then:
            builder.toString() == responseBuilder.toString()
    }

    def 'appendMasked masks the value defined in maskedFields'() {
        given:
            filter.maskFields = ['KEY1', 'KEY2']
            StringBuilder builder = new StringBuilder()
            String key = 'KEY1'
            String value = 'VALUE1'
            String delimiter = ':'
        when:
            StringBuilder responseBuilder = filter.appendMasked(builder, key, value, delimiter)
        then:
           'KEY1:*********' == responseBuilder.toString()
    }

    def 'getHeaders returns REQUEST headers concatenated together'() {
        when:
            String headers = filter.getHeaders(request)
        then:
            request.headerNames >> Collections.enumeration(['key1', 'key2'])
            request.getHeader('key1') >> 'value1'
            request.getHeader('key2') >> 'value2'

            'key1:value1, key2:value2' == headers
    }

    def 'getHeaders returns RESPONSE headers concatenated together'() {
        when:
            String headers = filter.getHeaders(response)
        then:
            response.getHeaderNames() >> ['key1', 'key2']
            response.getHeader('key1') >> 'value1'
            response.getHeader('key2') >> 'value2'

            'key1:value1, key2:value2' == headers
    }

    def 'getClientProtocol returns the value from the header X-Forwarded-Proto'() {
        when:
            String proto = filter.getClientProtocol(request)
        then:
            request.getHeader('X-Forwarded-Proto') >> 'https'
            'https' == proto
    }

    def 'getClientProtocol returns the response protocol when the header X-Forwarded-Proto is UNKNOWN'() {
        when:
            String proto = filter.getClientProtocol(request)
        then:
            request.getHeader('X-Forwarded-Proto') >> 'UNKNOWN'
            request.protocol >> 'https'
            'https' == proto
    }

    def 'getClientProtocol returns the response protocol when no header X-Forwarded-Proto is found'() {
        when:
           String proto = filter.getClientProtocol(request)
        then:
            request.protocol >> 'https'
            'https' == proto
    }

    def 'getClientIpAddress returns the value from the header X-Forwarded-For'() {
        when:
            String proto = filter.getClientIpAddress(request)
        then:
            request.getHeader('X-Forwarded-For') >> '192.168.1.10'
            '192.168.1.10' == proto
    }

    def 'getClientIpAddress returns the response protocol when the header X-Forwarded-For is UNKNOWN'() {
        when:
            String proto = filter.getClientIpAddress(request)
        then:
            request.getHeader('X-Forwarded-For') >> 'UNKNOWN'
            request.remoteAddr >> '192.168.1.10'
            '192.168.1.10' == proto
    }

    def 'getClientIpAddress returns the response protocol when no header X-Forwarded-For is found'() {
        when:
            String proto = filter.getClientIpAddress(request)
        then:
            request.remoteAddr >> '192.168.1.10'
            '192.168.1.10' == proto
    }

    def 'getBody converts bytes to a string'() {
        given:
            String contentType = 'none'
            byte[] buf = 'Test string'.bytes
            String charsetName = 'UTF-8'
        when:
            String content = filter.getBody(contentType, buf, charsetName)
        then:
            'Test string' == content
    }

    def 'getBody masks password field for JSON content type'() {
        given:
            String contentType = 'application/json'
            byte[] buf = '{"password":"secret","username":"tester"}'.bytes
            String charsetName = 'UTF-8'
        when:
            String content = filter.getBody(contentType, buf, charsetName)
        then:
            '{"password":"*********","username":"tester"}' == content
    }

    def 'getBody masks password field for FORM content type'() {
        given:
            String contentType = 'application/x-www-form-urlencoded'
            byte[] buf = 'password=secret&username=tester'.bytes
            String charsetName = 'UTF-8'
        when:
            String content = filter.getBody(contentType, buf, charsetName)
        then:
            'password=*********&username=tester' == content
    }

    def 'getUserPrincipal from request attribute'() {
        when:
            String userPrincipal = filter.getUserPrincipal(request)
        then:
            request.getAttribute(HttpActionLogFilter.USER_PRINCIPAL_KEY) >> 'client-super'
            'client-super' == userPrincipal
    }

    def 'getUserPrincipal from Basic Authentication header if not found in the request attributes'() {
        when:
            String userPrincipal = filter.getUserPrincipal(request)
        then:
            request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            'client-super' == userPrincipal
    }

    def 'getUserPrincipal from FORM parameters if not found anywhere else'() {
        given:
            filter.formUsernameFields = ['username']
        when:
            String userPrincipal = filter.getUserPrincipal(request)
        then:         
            request.getParameter('username') >> 'client-super'
            'client-super' == userPrincipal
    }

    def 'doFilterInternal is not filterable on /resources/test'() {
        given:
            filter.excludeResourcePaths = ['/resources/**']
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.servletPath >> '/resources/test'
            0 * request.getHeader('X-Forwarded-For')
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal is filterable and captures a complete request and response'() {
        given:
            int saveDetachedCount = 0
            String requestBodyJSON = '{"id":213213213, "amount":222}'
            byte[] requestBodyBytes = requestBodyJSON.getBytes(StandardCharsets.UTF_8)

            // Storing the request body can only be tested via Integration test
            filter.isStoreRequestBody = false
            filter.isStoreResponseBody = false

        when:
            filter.doFilterInternal(request, response, filterChain)

        then:
            // REQUEST SAVE
            1 * request.servletPath >> '/api'
            2 * request.pathInfo >> '?param1=valueA'
            1 * request.remoteAddr >> '127.0.0.1'
            1 * request.protocol >> 'http'
            1 * request.method >> 'GET'
            1 * request.requestURL >> new StringBuffer('http://test.request.url/api')
            2 * request.queryString >> 'param1=valueA'

            1 * request.headerNames >> Collections.enumeration(['test1','test2'])
            1 * request.getHeader('test1') >> 'value1'
            1 * request.getHeader('test2') >> 'value2'

            // RESPONSE SAVE
            1 * request.getAttribute(HttpActionLogFilter.CLIENT_KEY) >> currentClient
            1 * request.getAttribute(HttpActionLogFilter.USER_KEY) >> currentUser
            1 * request.getAttribute('HTTP_ACTION_LOG_USER_PRINCIPAL') >> 'test-user'

            1 * response.status >> 200
            1 * response.headerNames >> ['test3','test4']
            1 * response.getHeader('test3') >> 'value3'
            1 * response.getHeader('test4') >> 'value4'
            1 * request.contentLength >> requestBodyBytes.size()

            // VALIDATE
            2 * actionLogService.saveDetached(*_) >> { args ->
                saveDetachedCount++

                ActionLog actionLog = (ActionLog)args[0]

                // VALIDATE THE REQUEST SAVE
                assert actionLog.clientIpAddress == '127.0.0.1'
                assert actionLog.clientProtocol == 'http'
                assert actionLog.httpMethod == 'GET'
                assert actionLog.url == 'http://test.request.url/api?param1=valueA'
                assert actionLog.requestHeaders == 'test1:value1, test2:value2'

                // VALIDATE THE RESPONSE SAVE
                if (saveDetachedCount == 2) {
                    assert actionLog.durationMs
                    assert actionLog.username == 'test-user'
                    assert actionLog.client == currentClient
                    assert actionLog.user == currentUser
                    assert actionLog.httpStatus == '200'
                    assert actionLog.responseHeaders == 'test3:value3, test4:value4'
                }

                return actionLog
            }
    }
}

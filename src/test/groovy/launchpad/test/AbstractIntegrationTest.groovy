package launchpad.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AbstractIntegrationTest extends Specification {

    @Autowired
    protected TestRestTemplate restTemplate

    @LocalServerPort
    protected int httpPort

    @Autowired
    protected SuperClient superClient

    @Autowired
    protected StandardClient standardClient

    protected <T> ResponseEntity<T> GET(String uri, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            return restTemplate.exchange(uri, HttpMethod.GET, authorize(testClient), responseType, Collections.EMPTY_MAP)
        }
        return restTemplate.getForEntity(uri, responseType)
    }

    protected <T> ResponseEntity<T> POST(String uri, Class<T> responseType) {
        return restTemplate.postForEntity(uri, null, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> POST(String uri, HttpEntity<T> httpEntity, Class<T> responseType, TestClient testClient) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> PUT(String uri, HttpEntity<T> httpEntity, Class<T> responseType, TestClient testClient = null) {
        if (testClient) {
            httpEntity = authorize(testClient, httpEntity)
        }
        return restTemplate.exchange(uri, HttpMethod.PUT, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    protected <T> ResponseEntity<T> DELETE(String uri, Class<T> responseType, TestClient testClient = null) {
        HttpEntity httpEntity = null
        if (testClient) {
            httpEntity = authorize(testClient)
        }
        return restTemplate.exchange(uri, HttpMethod.DELETE, httpEntity, responseType, Collections.EMPTY_MAP)
    }

    private <T> HttpEntity authorize(TestClient testClient, HttpEntity<T> httpEntity = null) {
        HttpHeaders httpHeaders = new HttpHeaders()
        if (httpEntity) {
            // HttpEntity locks existing headers, so convert the unmodifiable set to a modifiable set.
            httpHeaders = enableWrite(httpHeaders)
        }
        httpHeaders.add('Authorization', "Bearer ${getAccessToken(testClient)}")

        // Create a new HttpEntity since the given one (if given) is immutable
        if (httpEntity?.body) {
            httpEntity = new HttpEntity<T>(httpEntity.body, httpHeaders)
        } else {
            httpEntity = new HttpEntity<>(httpHeaders)
        }

        return httpEntity
    }

    protected String getAccessToken(TestClient testClient) {
        MultiValueMap<String, String> credentials = new LinkedMultiValueMap<String, String>()
        credentials.client_id = testClient.clientId
        credentials.client_secret = testClient.clientSecret
        credentials.grant_type = 'client_credentials'

        HttpHeaders headers = new HttpHeaders()
        headers.add('Content-Type', MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(credentials, headers)

        ResponseEntity<Map> responseEntity = restTemplate
                .withBasicAuth(testClient.clientId, testClient.clientSecret)
                .postForEntity('/oauth/token', httpEntity, Map)

        assert responseEntity.statusCode.value() == 200

        return responseEntity.body.access_token
    }

    private static HttpHeaders enableWrite(HttpHeaders httpHeaders) {
        Set readOnlyHeaders = httpHeaders.entrySet()
        Map<String, List<String>> writeableHeaders = new LinkedCaseInsensitiveMap<List<String>>(readOnlyHeaders.size(), Locale.ENGLISH)
        for (Map.Entry<String, List<String>> entry : readOnlyHeaders) {
            writeableHeaders.put(entry.key, entry.value)
        }
        return writeableHeaders
    }
}

interface TestClient {
    String getClientId()
    String getClientSecret()
}

@Component
class SuperClient implements TestClient {
    @Value('${security.test.clients.super-client.client-id}')
    String clientId

    @Value('${security.test.clients.super-client.client-secret}')
    String clientSecret
}

@Component
class StandardClient implements TestClient {
    @Value('${security.test.clients.standard-client.client-id}')
    String clientId

    @Value('${security.test.clients.standard-client.client-secret}')
    String clientSecret
}

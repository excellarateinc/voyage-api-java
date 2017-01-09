package launchpad.security

import launchpad.test.AbstractIntegrationTest
import org.apache.http.Header
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import spock.lang.Ignore

/**
 * TODO Work in progress...    -- Tim Michalski 1/9/2017
 */
@Ignore
class OAuth2ImplicitIntegrationSpec extends AbstractIntegrationTest {

    def 'OAuth2 implicit authentication with authorization returns valid token'() {
        given:
            HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build()

        // Post to /authorize with "implicit" grant and client credentials w/ redirect URI
        // -- expect a redirect to the login page
        // -- expect a JSESSIONID cookie to pass into the next request
        when:
            List<NameValuePair> postBody = []
            postBody.add(new BasicNameValuePair('client_id', superClient.clientId))
            postBody.add(new BasicNameValuePair('redirect_uri', 'http://localhost:8080/oauth'))
            postBody.add(new BasicNameValuePair('response_type', 'token'))

            HttpPost httpPost = new HttpPost("http://localhost:${httpPort}/oauth/authorize")
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))

            CloseableHttpResponse response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/login') > 0
            Header sessionCookie = response.getFirstHeader('Set-Cookie')
            sessionCookie.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Post to the /login page (Form post) with user credentials
        // -- set cookie JSESSIONID from prior request
        // -- expect a redirect to the authorize page
        when:
            postBody = []
            postBody.add(new BasicNameValuePair('username', 'super'))
            postBody.add(new BasicNameValuePair('password', 'password'))

            httpPost = new HttpPost("http://localhost:${httpPort}/login")
            //httpPost.addHeader('Cookie', sessionCookie.value)
            httpPost.setEntity(new UrlEncodedFormEntity(postBody))
            response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 302
            response.getFirstHeader('Location').value.indexOf('/oauth/authorize') > 0
            Header sessionCookie2 = response.getFirstHeader('Set-Cookie')
            sessionCookie2.value.indexOf('JSESSIONID') >= 0
            response.close()

        // Get the /authorize page (following the redirect response)
//        when:
//            HttpGet httpGet = new HttpGet("http://localhost:${httpPort}/oauth/authorize")
//            response = httpClient.execute(httpGet)
//
//        then:
//            response.statusLine.statusCode == 302

        // Post to the /authorize page (Form post) with "accepted"
        // -- set cookie JSESSIONID from prior request
        // -- expect a JSON response with a token
        when:
            postBody = []
            postBody.add(new BasicNameValuePair('user_oauth_approval', 'true'))

            httpPost = new HttpPost("http://localhost:${httpPort}/oauth/authorize")

            httpPost.setEntity(new UrlEncodedFormEntity(postBody))
            response = httpClient.execute(httpPost)

        then:
            response.statusLine.statusCode == 200

            response.close()

//        when:
//            MultiValueMap<String, String> postBody = new LinkedMultiValueMap<String, String>()
//            postBody.client_id = superClient.clientId
//            postBody.redirect_uri = 'http://localhost:8080/oauth'
//            postBody.response_type = 'token'
//
//            HttpHeaders headers = new HttpHeaders()
//            headers.add('Content-Type', MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        //HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(postBody, headers)

        // TestRestTemplate myRestTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES, TestRestTemplate.HttpClientOption.ENABLE_REDIRECTS)

//            RestTemplate myRestTemplate = new RestTemplate(new SimpleClientHttpRequestFactory() {
//                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
//                    super.prepareConnection(connection, httpMethod)
//                    connection.
//                    connection.setInstanceFollowRedirects(true)
//                }
//            })

//            HttpEntity<String> response = myRestTemplate.exchange("http://localhost:${httpPort}/oauth/authorize", HttpMethod.POST, httpEntity, String)
//
//        then:
//            response.statusCode.value() == 302
//            response.headers.getFirst('Location').indexOf('/login') > 0
//
//        when:
//            postBody = new LinkedMultiValueMap<String, String>()
//            postBody.username = 'super'
//            postBody.password = 'password'
//
//            httpEntity = new HttpEntity<MultiValueMap<String, String>>(postBody, headers)
//            response = myRestTemplate.exchange("http://localhost:${httpPort}/login", HttpMethod.POST, httpEntity, String)
//
//        then:
//            response.statusCode.value() == 302
//            //response.headers.getFirst('Location').indexOf('/') == 0
//
//        when:
//            postBody = new LinkedMultiValueMap<String, String>()
//            postBody.client_id = superClient.clientId
//            postBody.redirect_uri = 'http://localhost:8080/oauth'
//            postBody.response_type = 'token'
//
//            httpEntity = new HttpEntity<MultiValueMap<String, String>>(postBody, headers)
//
//            response = myRestTemplate.exchange("http://localhost:${httpPort}/oauth/authorize", HttpMethod.POST, httpEntity, String)
//        then:
//            response.statusCode.value() == 302
//            response.headers.getFirst('Location').indexOf('/') == 0
    }

    def 'OAuth2 implicit authentication without authorization returns valid token'() {

        // Setup the super client to bypass user authorization

        // Post to /authorize with "implicit" grant and client credentials w/ redirect URI
        // -- expect a redirect to the login page

        // Post to the /login page (Form post) with user credentials
        // -- get/set cookie JSESSIONID from prior request
        // -- expect a JSON response with a token

    }
}

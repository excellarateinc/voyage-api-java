## Security
Overview of the Security features and configurations that have been implemented within the Voyage API. 

## Table of Contents
* [Secure Programming](#secure-programming)
* Security Features
  - [Authentication: OAuth2 (default)](#authentication-oauth2-default)
  - Authentication: User Credentials
  - Authorization: Permission Based
  - 2-Factor Authentication
  - [Cross Origin Resource Sharing (CORS)](#cross-origin-resource-sharing-cors)
  - [Cross Site Request Forgery (CSRF)](#cross-site-request-forgery-csrf)
  - Forgot Password
  - [OWASP Top 10](#owasp-top-10)
  - Password Policy
  - User Verification
* [Security Configuration](#security-configuration)
  - CORS 
  - Environment Specific Application Properties
  - JWT Public/Private Key
  - Public Resources
  - User Verification
* Audit Logging
  - Action Logs
  - Change Logs

## Secure Programming
The majority of technology security breaches that occur are through software applications. As developers create new software they need to be very mindful of secure programming principals in order to protect the users and companies that use the software. A developer needs to simply Google "Secure Coding" or "Secure Programming" to find many articles on best practices for secure programming. We've included a number of references below that are a great start. 

All programmers working on this app should at least read through the reference materials and take the introductory courses on secure programming offered by the [OWASP Academy](http://owasp-academy.teachable.com) with course title 'OWASP AppSec Tutorials' by Jerry Hoff. While the Voyage API lays down many protections within the frameworks and features it uses, every developer is responsible for understanding why these frameworks were put in place, how these frameworks fortify security, and what to do in their own code when they are building new features. 

#### References
* https://www.owasp.org/index.php/OWASP_Secure_Coding_Practices_-_Quick_Reference_Guide
* https://www.securecoding.cert.org/confluence/display/seccode/Top+10+Secure+Coding+Practices
* https://www.securecoding.cert.org/confluence/display/java/Java+Coding+Guidelines 
* http://owasp-academy.teachable.com
* https://www.lynda.com/Programming-Foundations-tutorials/Techniques-Developing-Secure-Software/418266-2.html

:arrow_up: [Back to Top](#table-of-contents)


## Security Features

### Authentication: OAuth2 (default)
![OAuth2 Implicit Authentication Workflow](./images/SECURITY_OAUTH2.png)

#### Overview
The default security configuration of Voyage API is OAuth2 with the Implicit Authentication and Client Credentials authenticaiton workflows implemented. OAuth2 was chosen as the default authentication mechanism over a simple username/password workflow because it provides a common pattern implemented by many enterprises, allows for a more secure login process when using Implicit Authentication, and enables the API to be a branded Authorization server should it desire to allow third-party apps to interface with its web services. 

Voyage API implements OAuth2 natively within the application using [Spring Security OAuth2](https://projects.spring.io/spring-security-oauth/docs/oauth2.html) framework. 

Walk through accessing secured web services using both Implicit Authentication and Client Credentials in the [Development: Access Secured Web Services](./DEVELOPMENT.md#access-secured-web-services) section. 

#### Authentication Server
The Authentication Server is an independent component of OAuth2 that is responsible for authenticating users and returning secure tokens for accessing the Resource Server. The Authentication Server can be a third-party entity (ie Google, Facebook) or a privately hosted server. Voyage API implements its own Authentication Server following the [Spring Security OAuth2](https://projects.spring.io/spring-security-oauth/docs/oauth2.html) defined structure.  

The configuration for the Authentication Server is discussed in more detail within the [Security Configuration](#security-configuration) section. The implementation of the Authentication Server can be found at `/src/main/groovy/voyage/config/OAuth2Config.groovy`. Within the config class, both of the Authorization Server and the Resource Server are defined. 

##### Highlights of the Authorization Server implementation:
```
@Configuration
class OAuth2Config {

    /**
     * Configures the OAuth2 Authorization server to use a custom ClientDetailsService and to govern access to
     * authorization endpoints.
     */
    @Configuration
    @EnableAuthorizationServer
    class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

        @Value('${security.jwt.key-store-filename}')
        private String keyStoreFileName

        @Value('${security.jwt.key-store-password}')
        private String keyStorePassword

        @Value('${security.jwt.private-key-name}')
        private String privateKeyName

        @Value('${security.jwt.private-key-password}')
        private String privateKeyPassword

        @Autowired
        private AuthenticationManager authenticationManager

        @Autowired
        private PermissionBasedClientDetailsService permissionBasedClientDetailsService

        @Autowired
        private WebResponseExceptionTranslator apiWebResponseExceptionTranslator

        @Bean
        JwtAccessTokenConverter accessTokenConverter() {
            KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(new ClassPathResource(keyStoreFileName), keyStorePassword.toCharArray())
            KeyPair keyPair = keyFactory.getKeyPair(privateKeyName, privateKeyPassword.toCharArray())
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter()
            converter.keyPair = keyPair
            return converter
        }

        @Override
        void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer

                // Expose the verifier key endpoint "/oauth/token_key" to the public for validation of the JWT token
                .tokenKeyAccess('permitAll()')

                // Require users to be authenticated before accessing "/oauth/check_token"
                .checkTokenAccess('isAuthenticated()')
        }

        @Override
        void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .authenticationManager(authenticationManager)
                    .accessTokenConverter(accessTokenConverter())
                    .exceptionTranslator(apiWebResponseExceptionTranslator)
        }

        @Override
        void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(permissionBasedClientDetailsService)
        }
    }
...
```
1. Extends the stock Spring Security OAuth2 framework
2. Implements stateless JSON Web Token (JWT) as the token provider with a changeable public/private key for encoding the token. 
   - See [Security Configuration](#security-configuration) for instructions on how to configure the JWT public/private key by environment
3. Implements a custom exception translator to ensure all expected or unexptected issues are handled consistently
4. Implements a custom Permission Based Client authorization service that grants access to resources based on the Permission records associated with their profile. 
   - See [Authorization: Permission Based](#authorization-permission-based) for more information. 
   - Inspect `/src/main/groovy/voyage/security/PermissionBasedClientDetailsService` for implementation details.

#### Resource Server
The Resource Server is an independent component of OAuth2 that is responsible for facilitating secured access to the web services provided by the API (ie HTTP GET /api/users). The Resource service is always implemented by the API application as it hosts the web services that comprise the API product. The Resource Server shouldn't need to know anything about the Authentication Server's location or how it operates other than the method by which to validate tokens and certify that they originated from the Authentication Server. Voyage API implements the Resource Server following the [Spring Security OAuth2](https://projects.spring.io/spring-security-oauth/docs/oauth2.html) defined structure.  

The configuration for the Resource Server is discussed in more detail within the [Security Configuration](#security-configuration) section. The implementation of the Resource Server can be found at `/src/main/groovy/voyage/config/OAuth2Config.groovy`. Within the config class, both of the Authorization Server and the Resource Server are defined. 

##### Highlights of the Resource Server config:
```
@Configuration
class OAuth2Config {
...

    @Configuration
    @EnableResourceServer
    class ResourceServerConfig extends ResourceServerConfigurerAdapter {
        private static final String ANY_PATH = '/**'
        private static final String API_PATH = '/api/**'
        private static final String READ = "#oauth2.hasScope('Read Data')"
        private static final String WRITE = "#oauth2.hasScope('Write Data')"

        @Value('${security.permitAll}')
        private String[] permitAllUrls

        @Autowired
        private WebResponseExceptionTranslator apiWebResponseExceptionTranslator

        @Override
        void configure(HttpSecurity http) throws Exception {
            http

                // Limit this Config to only handle /api requests. This will also disable authentication filters on
                // /api requests and enable the OAuth2 token filter as the only means of stateless authentication.
                .requestMatchers()
                    .antMatchers(API_PATH)
                    .and()

                // Bypass URLs that are public endpoints, like /api/v1/forgotPassword
                .authorizeRequests()
                    .antMatchers(permitAllUrls).permitAll()
                    .and()

                // Enforce client 'scope' permissions on all authenticated requests
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, ANY_PATH).access(READ)
                    .antMatchers(HttpMethod.POST, ANY_PATH).access(WRITE)
                    .antMatchers(HttpMethod.PUT, ANY_PATH).access(WRITE)
                    .antMatchers(HttpMethod.PATCH, ANY_PATH).access(WRITE)
                    .antMatchers(HttpMethod.DELETE, ANY_PATH).access(WRITE)
                    .and()
        }

        @Override
        void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources
                // Override exception formatting by injecting the accessDeniedHandler & authenticationEntryPoint
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint())
        }
        ...
    }
}
```
1. Extends the stock Spring Security OAuth2 framework
2. Implements a stateless JWT token for authentication
3. Intercepts all `/api` requests and authenticates the user based on the JWT token
   - All other requests are ignored and naturally picked up by the base Spring Security frameowrk for processing.
   - Base Spring Security implementation is located at `/src/main/groovy/voyage/security/WebSecurityConfig.groovy`
4. Defaults all `/api` requests to be secured
5. Allows for exposure of publicly accessible `/api` access by specifying public URL paths in an external configuration file
6. Implements a custom exception translator to ensure all expected or unexptected issues are handled consistently
7. Enforces client Authorization based on the grants associated with their profile
   - The only grants supported are 'READ' and 'WRITE'
   - READ grant maps to HTTP GET
   - WRITE grant maps to HTTP POST, PUT, PATCH, DELETE
   - Client's that do not have these grants configured in their profile stored in the database cannot perform these operations. 

#### Implicit Authentication
An authentication workflow that essentially is initiated by the client app (ie mobile app with embedded client ID) where the end-user is transfered over to the server-side authentication form(s). The Authentication server will validate the incoming client ID (no password is given), and then facilitate one or more secure login pages that accept and validate the end-user. Once the end-user authentication has completed successfully, then the user is redirected back to the client using the "redirect_url" from the client's profile in the database. Redirecting the user back to the client URL is a safe and secure way of getting the user back to a known / registered app. 

> NOTE: The Spring Security implicit authentication currently supports multiple redirect URLs. OAuth2 requires that the client provide the redirect_url in the initial hand-off of the end-user. Spring Security OAuth2 framework will validate that the given redirect_url matches a value within the client's profile in the database. If the given redirect_url value doesn't match a client redirect_url in the database exactly, then the authentication process will throw an error and stop. 

Key points:

1. The user instructs the client 'app' to make API requests on the user's behalf. 
2. The client initiates the authentication using their client ID, but does not provide a password because the user will be required to enter their own username and password to authorize the client. 
3. The API will load both the Client and User objects into the session
4. This authentication method is the preferred method for a web or mobile app

Walk through accessing secured web services using both Implicit Authentication and Client Credentials in the [Development: Access Secured Web Services](./DEVELOPMENT.md#access-secured-web-services) section. 

#### Client Credentials
A server-to-server authentication workflow where a client passes to the authenticaiton server a Client ID and Client Secret to authenticate. Upon successful authentication, the client is given an access token that can be used for web service requests. The Client Credentials workflow should only be used in situations where the client can guarantee secure storage of the Client Secret, which would reserve this communication method to server-side integration with the authentication server. Client-side apps, like Javascript clients (AngularJS), native mobile apps, or hybrid mobile apps are not considered secure and should not have an embedded Client Secret within the source code. Therefore, client apps should NOT use Client Credentials as an authentication method. 

> NOTE: Some might argue that compiling a secure password into the native mobile app binary and deploying that to a mobile device is secure. This is definitely not the case! There are many examples on the Internet on [decompiling Java/Android](https://infosecguide.wordpress.com/2013/12/17/step-by-step-guide-to-decompiling-android-apps/) and [iOS native mobile apps](http://reverseengineering.stackexchange.com/questions/4096/decompiling-iphone-app) to reveal source code and to grab passwords. 

Key points:

1. The client accesses the API directly without a user and uses a secure password to authenticate.
2. The client is the only actor using the API and must provide a client ID and password
3. The API will not load a User object into the session unless the client ID maps to a User username
4. API services that require a User object loaded into memory will not function with this authentication method
5. This authentication method is reserved for testing and for server-to-server exchanges

Walk through accessing secured web services using both Implicit Authentication and Client Credentials in the [Development: Access Secured Web Services](./DEVELOPMENT.md#access-secured-web-services) section. 

:arrow_up: [Back to Top](#table-of-contents)


### Cross Origin Resource Sharing (CORS)
#### Overview
CORS is a feature built into web browsers and web servers that allow for bi-directional communication on the allowance for a web page to make calls to other servers other than the originator of the content. Browsers have for a long time restricted web sites from making calls out to sites that are not from the web page origin. 

Voyage API implements server-side CORS instructions for consumers operating out of web browsers, such as an AngularJS app. 

#### CORS Vulnerabilities
Even though CORS provides valuable protection from hackers, it also exposes a fundamental architecture flaw that hackers are able to exploit. The 'CORS Abuse' reference link below describes the situation in detail. In short, a hacker can send to the web server an Origin header value containing any information that the hacker wants to send. For example:
```
Origin: imahacker.com
```
When the server application enables `Access-Control-Allow-Credentials: true`, then the CORS spec doesn't allow a public wildcard `Access-Control-Allow-Origin: *`, instead the server must return back a specific domain. Many HTTP Servers and frameworks that offer CORS support (include Spring Security CORS add-on), will simply echo back the value that is provided in the `Origin` request header. Anytime a server echos back values given to it, that echo'd response becomes a hacker foothold for all sorts of mischief. 

#### Custom CORS Filter for Voyage
Voyage API provides it's own implementation of the CORS filter at `/src/main/groovy/voyage/security/CorsServletFilter`. Features of this custom CORS filter are:
* Integrated with the OAuth 'client' invoking the request
* if the 'client' requesting access to the API is authenticated, then the given Origin on the request is matched to the Client Origins in the database (client_origin table)
  - if a match is found, then return the value _in the database_ as the value for `Access-Control-Allow-Origin` header response
  - if no match is found, then default to being permissive and return a public wildcard `Access-Control-Allow-Origin: *`
* if the request is anonymous (client not logged in)
  - default to being permissive and return a public wildcard `Access-Control-Allow-Origin: *`

> NOTE: Defaulting to permissive origin in CorsServletFilter because an assumption is made that the security framework will catch unauthorized requests and prevent access. For a more restrictive implementation, consider extending this class or replacing it with a different implementation.

#### References
* [OWASP: Cross Origin Resource Sharing - Origin Header Scrutiny](https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny)
* [CORS Abuse](https://blog.secureideas.com/2013/02/grab-cors-light.html)


:arrow_up: [Back to Top](#table-of-contents)


### Cross-Site Request Forgery (CSRF)
#### Overview
CSRF is the ability of a hacker to hijack session information stored within a web browser by invoking a request to the website where the session information was generated. The hijacker may not be able to access the session information in the browser, but they can impersonate a prior session and get valuable information back from the website. For example, if the end-user logged into a banking website and a Session Cookie was pushed to the end-user's browser to keep them logged in, then a hijacker could invoke banking requests as an authenticated user without having to know the user's login credentials. 

The common way to thwart this attack is by including a web server generated code that is embedded into each page displayed to the end-user. When the user submits information back to the server, the web server generated code must be given back to the server where it is validated before any actions are processed.

Web service APIs are typicaly single transactions, in fact, good APIs strive to be a simple request/response to complete a task. Requiring a consumer to call a web service to get a CSRF token to then submit to another web service request seems a bit much. Even still, if a web service API maintains state between web service requests via a Cookie or persistent Basic Auth, then a web service is open to a possible CSRF attack. 

The initial construction of Voyage API strongly discourages the use of the Servlet Session or anything that would retain state beyond the HTTP Request. The current authentication and authorization of the `/api` resource server uses JWT tokens transmitted through the HTTP Request Headers, which must be placed into the header for each request. No Cookies are supported in the `/api` resource server and HTTP Basic Auth is disabled. 

Given the architecture of Voyage API, no CSRF controls are built into the API. Please revise this section if the web services API for this app requires the use of Cookies and/or Sessions that span multiple requests. 

#### References
* [OWASP: CSRF Prevention Sheet](https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet)

:arrow_up: [Back to Top](#table-of-contents)


## User 'Forgot Password' Pattern
* Why did we go the approach we did. 
* References to OWASP and other security sources

## 2-Factor Authentication
* User Verification Servlet Filter
* How to exlclude resources from this filter

## Tokens
* Since we are using JWT, we don't have an easy way to revoke tokens other than the expiration date since we do not store these tokens in the database. 
* If someone somehow steals the token, they can impersonate that user completely. 
* Keeping a short expiration on the token is a way to force the client to "re up", typically using the stored refresh token. 
* What is the default TTL for the tokens for the API? 
  * Long Lived Tokens
    * 60 day before expiration
    * Refresh tokens are given out and can be used to get a new access token
    * Mobile apps get long-lived-tokens in Facebook
  * Short Lived Tokens
    * 1-2 hours before expiration
    * Refresh tokens are given out and can be used to get a new access token
    * Web apps get short-lived-tokens in Facebook
    * Short lived tokens can be exchanged for long lived tokens. The implementor can essentially decide.
* Tokens
  * User Tokens: /authorize grant=token
  * Client/App Token: /token (w/ client_id & secret)
  * Refresh Tokens
  * Following Facebook's explanations: https://developers.facebook.com/docs/facebook-login/access-tokens

## Setup

### Configure Spring OAuth with JWT & asymmetric RSA keypair
Following the example found in https://beku8.wordpress.com/2015/03/31/configuring-spring-oauth2-with-jwt-asymmetric-rsa-keypair/

#### Generate Private/Public keys for OAUTH2 JWT
```
keytool -genkeypair -alias jwt -keyalg RSA \
-dname "CN=Web Server,OU=Unit,O=Organization,L=City,S=State,C=US" \
-keypass changeme -keystore jwt.jks -storepass changeme
```

* Revise the keytool statement above with your own personalized parameters
* Copy the jwt.jks to your /src/main/resources folder so that it is available on the classpath

> NOTE: These are the default settings. Be sure to document any changes in the "keypass" or "storepass" in a _secure location_
(ie not .MD files in source control) so that you don't lose these!  

#### Export the Public Key
```
keytool -export -keystore jwt.jks -alias jwt -file jwt.cer
```
* Enter the password used to generate the keystore (ie changeme)
* The key will be exported to jwt.cer
 
```
openssl x509 -inform der -in jwt.cer -pubkey -noout
```

The output will be the public key, which should look something like:

```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4REj5EYufU5OUnv9nij+
j9irwALL3BwX9XxB7oDx3uj93P5h8rzTTdG/suaG3aBqRr5rqXpmTgwG1nf6FBfR
8kiPp9R196cAT9g4OInsdNbux7oy5akUVsRo9pagEL0JB7eGbASi0z5A38QkpbjB
MhIN0W9zwghsGNbf7N6wTVQN1NFHDW9zMdWUS9VBPeEGUZAMkKElGltHVhCdJGBf
OdriLIO2KdimjO5q9Q9+qG2B96DFGNYvmuDlDLM11Q2fsre305CV1HN0vQulLhlr
MJo9QdZt1g2d1VN5uIKid5dxWTAuUvJhgla6yCaTfYeV1OGq5C3DFV7tKDGNAIXL
TQIDAQAB
-----END PUBLIC KEY-----
```

Copy the public key into the /src/main/resources/application.yaml file along with the JWT keystore
and private key information, like:

```
security:
  oauth2:
    resource:
      id: voyage
      jwt:
        key-value: |
          -----BEGIN PUBLIC KEY-----
          MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4REj5EYufU5OUnv9nij+
          j9irwALL3BwX9XxB7oDx3uj93P5h8rzTTdG/suaG3aBqRr5rqXpmTgwG1nf6FBfR
          8kiPp9R196cAT9g4OInsdNbux7oy5akUVsRo9pagEL0JB7eGbASi0z5A38QkpbjB
          MhIN0W9zwghsGNbf7N6wTVQN1NFHDW9zMdWUS9VBPeEGUZAMkKElGltHVhCdJGBf
          OdriLIO2KdimjO5q9Q9+qG2B96DFGNYvmuDlDLM11Q2fsre305CV1HN0vQulLhlr
          MJo9QdZt1g2d1VN5uIKid5dxWTAuUvJhgla6yCaTfYeV1OGq5C3DFV7tKDGNAIXL
          TQIDAQAB
          -----END PUBLIC KEY-----

  # FOR PRODUCTION: The following MUST be overridden to ensure secrecy of the passwords for the keystore and private
  # See where you can override at https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
  jwt:
    key-store-filename: jwt.jks
    key-store-password: changeme
    private-key-name: jwt
    private-key-password: changeme       
```


### OWASP Top 10
#### Overview
The most recent Open Web Association of Secure Programmers (OWASP) top 10 most exploited custom app vulnerabilities are as follows:

1. Injection
2. Weak authentication and session management
3. XSS
4. Insecure Direct Object References
5. Security Misconfiguration
6. Sensitive Data Exposure
7. Missing Function Level Access Control
8. Cross Site Request Forgery
9. Using Components with Known Vulnerabilities
10. Unvalidated Redirects and Forwards

#### 1. Injection
* Hibernate SQL/HQL Injection concerns
  - Uses JPA with SQL/HQL parameter being applied to the SQL/HQL query only through Hibernate supplied setters()
  - Hibernate query parameter setters() assume all content is insecure and escapes all characters that would conflict with the sytax of the query. 

#### 2. Weak authentication and session management

#### 3. XSS

#### Resources
* [OWASP Top 10 Cheat Sheet](https://www.owasp.org/index.php/OWASP_Top_Ten_Cheat_Sheet)

:arrow_up: [Back to Top](#table-of-contents)


## Stateless Server Authentication
JWT provides for for stateless authentication so that we don't have to worry about storing the token in the backend server. This should avoid having to do OAuth2 token storage. 

## Role Based Access Control

### Actors (aka Users) 

### Roles
Convention: role.super, role.admin, role.doctor, role.nurse

### Permissions
Convention: api.roles.list, api.roles.get, api.roles.create, api.roles.update, api.roles.delete
api.user.list, api.user.get, api.user.create, api.user.update, api.user.delete

api.account.get, api.account.update

## OAuth2
Properties
* URL: /api/oauth/token
* Post Body: grant_type=client_credentials
* Client ID: my-client-with-secret
* Secret: secret

Example Token Generation:

```
curl -H "Accept: application/json" my-client-with-secret:secret@localhost:8080/oauth/token -d grant_type=client_credentials
```

Apply token to request header

```
Authorization: Bearer caaafafd-08bb-4b83-b9cc-a3b78e500e91
```

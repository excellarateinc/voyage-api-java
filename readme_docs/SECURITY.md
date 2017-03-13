## Security
Overview of the Security considerations and configurations that have been implemented within the Voyage API. 

## Table of Contents
* Security Patterns
  - Authentication: OAuth2 (default)
  - Authentication: User Credentials
  - Authorization: Permission Based
  - 2-Factor Authentication
  - [Cross Origin Resource Sharing (CORS)](#cross-origin-resource-sharing-cors)
  - [Cross Site Request Forgery (CSRF)](#cross-site-request-forgery-csrf)
  - Forgot Password
  - User Verification
* Security Configuration
  - CORS 
  - Environment Specific Application Properties
  - JWT Public/Private Key
  - Public Resources
  - User Verification
* Audit Logging
  - Action Logs
  - Change Logs

## Security Patterns

## Cross Origin Resource Sharing (CORS)
[OWASP: Cross Origin Resource Sharing - Origin Header Scrutiny](https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny)

[CORS Abuse](https://blog.secureideas.com/2013/02/grab-cors-light.html)

CORS is a feature built into web browsers and web servers that allow for bi-directional communication on the allowance for a web page to make calls to other servers other than the originator of the content. Browsers have for a long time restricted web sites from making calls out to sites that are not from the web page origin. 

Voyage API implements server-side CORS instructions for consumers operating out of web browsers, such as an AngularJS app. 

Even though CORS provides valuable protection from hackers, it also exposes a fundamental architecture flaw that hackers are able to exploit. The 'CORS Abuse' link at the top of this section describe the situation in detail. In short, a hacker can send to the web server an Origin header value containing any information that the hacker wants to send. For example:
```
Origin: imahacker.com
```
When the server application enables `Access-Control-Allow-Credentials: true`, then the CORS spec doesn't allow a public wildcard `Access-Control-Allow-Origin: *`, instead the server must return back a specific domain. Many HTTP Servers and frameworks that offer CORS support (include Spring Security CORS add-on), will simply echo back the value that is provided in the `Origin` request header. Anytime a server echos back values given to it, that echo'd response becomes a hacker foothold for all sorts of mischief. 

Voyage API provides it's own implementation of the CORS filter at `/src/main/groovy/voyage/security/CorsServletFilter`. Features of this custom CORS filter are:
* Integrated with the OAuth 'client' invoking the request
* if the 'client' requesting access to the API is authenticated, then the given Origin on the request is matched to the Client Origins in the database (client_origin table)
  - if a match is found, then return the value _in the database_ as the value for `Access-Control-Allow-Origin` header response
  - if no match is found, then default to being permissive and return a public wildcard `Access-Control-Allow-Origin: *`
* if the request is anonymous (client not logged in)
  - default to being permissive and return a public wildcard `Access-Control-Allow-Origin: *`

> NOTE: Defaulting to permissive origin in CorsServletFilter because an assumption is made that the security framework will catch unauthorized requests and prevent access. For a more restrictive implementation, consider extending this class or replacing it with a different implementation.

:arrow_up: [Back to Top](#table-of-contents)


### Cross-Site Request Forgery (CSRF)
[OWASP: CSRF Prevention Sheet](https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet)

CSRF is the ability of a hacker to hijack session information stored within a web browser by invoking a request to the website where the session information was generated. The hijacker may not be able to access the session information in the browser, but they can impersonate a prior session and get valuable information back from the website. For example, if the end-user logged into a banking website and a Session Cookie was pushed to the end-user's browser to keep them logged in, then a hijacker could invoke banking requests as an authenticated user without having to know the user's login credentials. 

The common way to thwart this attack is by including a web server generated code that is embedded into each page displayed to the end-user. When the user submits information back to the server, the web server generated code must be given back to the server where it is validated before any actions are processed.

Web service APIs are typicaly single transactions, in fact, good APIs strive to be a simple request/response to complete a task. Requiring a consumer to call a web service to get a CSRF token to then submit to another web service request seems a bit much. Even still, if a web service API maintains state between web service requests via a Cookie or persistent Basic Auth, then a web service is open to a possible CSRF attack. 

The initial construction of Voyage API strongly discourages the use of the Servlet Session or anything that would retain state beyond the HTTP Request. The current authentication and authorization of the `/api` resource server uses JWT tokens transmitted through the HTTP Request Headers, which must be placed into the header for each request. No Cookies are supported in the `/api` resource server and HTTP Basic Auth is disabled. 

Given the architecture of Voyage API, no CSRF controls are built into the API. Please revise this section if the web services API for this app requires the use of Cookies and/or Sessions that span multiple requests. 

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

#### 

## OWASP

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

## Security
Overview of the Security considerations and configurations that have been implemented within the Voyage API. 

## Table of Contents
* Security Patterns
  - Authentication: OAuth2 (default)
  - Authentication: User Credentials
  - Authorization: Permission Based
  - 2-Factor Authentication
  - Cross Origin Resource Sharing
  - Cross Site Request Forgery (CSRF)
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

## User 'Forgot Password' Pattern
* Why did we go the approach we did. 
* References to OWASP and other security sources

## 2-Factor Authentication
* User Verification Servlet Filter
* How to exlclude resources from this filter

## CORS
* What is it and how to override it by environment
  * 
* Ionic apps are not affected since the local browser that runs the app loads resources using file://, which doesn't enforce CORS.
  * When running Ionic in emulate mode, the app resources will be loaded with http://, which will enforce CORS
  * http://blog.ionic.io/handling-cors-issues-in-ionic/

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

### Cross-Site Request Forgery (CSRF)
Provide an example or link to the OWASP cheat sheet for CSRF (https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet)

Voyage API uses JWT that is transmitted through HTTP Request Headers, no Cookies used. HTTP Basic Auth is disabled. 


Discuss how the CSRF token is generated and what happens if/when it expires, or if/when it is invalid (401 Access Denied. CSRF token was not valid)
* Get the token from the response header and retry the request
* Token is alive for the duration of the session, so if the session invalidates on the server or if the user is routed to a different web server instance (via load balancer), then 

X-CSRF-TOKEN is provided on every response

X-CSRF-TOKEN is required on any "save" HTTP methods: POST, PUT, PATCH, DELETE.

/getCsrfToken web service endpoint is available to obtain a valid token when the X-CSRF-TOKEN is not available. 

Walk through some examples of how to use this when saving. 

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

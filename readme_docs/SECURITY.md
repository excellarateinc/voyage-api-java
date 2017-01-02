## Setup
1. Generate Private/Public keys for OAUTH2 /src/main/resources/application.yaml

## Tokens
* Since we are using JWT, we don't have an easy way to revoke tokens other than the expiration date since we do not store these tokens in the database. 
* If someone somehow steals the token, they can impersonate that user completely. 
* Keeping a short expiration on the token is a way to force the client to "re up", typically using the stored refresh token. 
* What is the default TTL for the tokens for the API? 
* Tokens
  * User Tokens: /authorize grant=token
  * Client/App Token: /token (w/ client_id & secret)
  * Following Facebook's explanations: https://developers.facebook.com/docs/facebook-login/access-tokens

## OWASP

### Cross-Site Request Forgery (CSRF)
Provide an example or link to the OWASP cheat sheet for CSRF (https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet)

Launchpad uses JWT that is transmitted through HTTP Request Headers, no Cookies used. HTTP Basic Auth is disabled. 


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

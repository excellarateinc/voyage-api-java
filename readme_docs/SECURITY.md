
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

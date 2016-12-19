
## Role Based Access Control

### Actors (aka Users) 

### Roles

Convention: ROLE_SUPER, ROLE_ADMIN, ROLE_DOCTOR, ROLE_REGISTERED_NURSE, ROLE_NURSE_PRACTITIONER

Convention: role.super_user, role.administrator, role.doctor, role.registered_nurse


### Permissions

Convention: PERMISSION_ROLES_LIST, permission.roles.list, permission.roles.get, permission.roles.create, permission.roles.update, permission.roles.delete

permission.account.get, permission.account.update

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

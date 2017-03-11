## Development Recipes
Instructional recipies for how to do something within the codebase. 

> __Keep Organized__ Keep the Table of Contents alphabetized and do your best to extend this document in a way that will be easy to read/scroll for all developers.

## Table of Contents
* [General](#general)
  - [APIDoc - Document A Web Service](#apidoc---document-a-web-service)
  - [Folder by Feature](#folder-by-feature)
  - [Run Tests](#run-tests)
* [Web Services](#web-services)
  - [API Versioning](#api-versioning)
  - [Consuming API Services](#consuming-api-services)
  - [Create a Web Service Endpoint Controller](#create-a-web-service-endpoint-controller)
  - [HTTP Request - Validate Request Data](#http-request---validate-request-data)
  - [Securing a Web Service endpoint](#securing-a-web-service-endpoint)
* [Service & Domain logic](#service--domain-logic)
  - [Create a Service](#creating-a-service)
  - Error Handling w/ i18n support
* [Build & Deploy](#build-deploy)
  - [Clean workspace and rebuild](#clean-workspace-and-rebuild)
* [Data Layer](#data-layer)
  - [Create a database access Repository](#create-a-database-access-repository)
  - [Add database structure changes](#add-database-structure-changes)
  
  
## General

### APIDoc - Document A Web Service
Web service documentation for consumers is facilitated through the use of a framework called [apiDoc](http://apidocjs.com). apiDoc provides a set of annotation that are placed in a comment block within the web service controller class. To generate the documentation website for consumers, apiDoc provides a Node script that scans the source files for apiDoc annotations to create a pretty HTML website. 

The complete documenation of apiDoc can be found on their website [http://apidocjs.com](http://apidocjs.com/).

#### Example
Below is an example of the comments used to document an endpoint.

```
 /**
  * @api {get} /hello Simple "hello" I'm alive endpoint
  * @apiVersion 1.0.0
  * @apiName HelloGet
  * @apiGroup Hello
  *
  * @apiPermission none
  *
  * @apiUse AuthHeader
  *
  * @apiSuccess {String} status
  * @apiSuccess {String} datetime
  *
  * @apiSuccessExample Success-Response:
  *   HTTP/1.1 200 OK
  *   [
  *       {
  *           "status": "alive",
  *           "datetime": "2016-12-23 17:55:55 UTC",
  *       }
  *   ]
  **/
  @GetMapping
  ResponseEntity list()
...
```

#### Reusable apiDoc blocks
apiDoc supports creating reusuable documentation blocks using [@apiDefine](http://apidocjs.com/#param-api-define). This 
cuts down on repeated comment blocks for shared elements such as errors. 
All reusable blocks should be placed in  ***apidoc/apidoc-header.js***

#### Current @apiDefine blocks

##### Headers
1. AuthHeader
    - Used when an API requires the authorization header 
    
##### Errors
1. BadRequestError
   - Used when an API can return a 400
2.  NotFoundError
    - Used when an API can return a 404
3. UnauthorizedError
    - Used when an API can generate a 401

##### Request Params
1. UserRequestModel
    - Used when an API takes a user as input

##### Response Params
1. UserSuccessModel
    - Used when an API returns a single user
   
#### Generating documentation
To generate the api docs after a change:

1. In ***apidoc/*** execute npm run doc
   - This is an npm script that is defined in package.json
   - This will scan the Controllers folder for endpoints and place the output in /docs

To view the documentation either run the application and navigate to /docs/ or open the static index.html file.

:arrow_up: [Back to Top](#table-of-contents)
  
  
 
### Folder by Feature

#### File and Folder Structure

Voyage uses a folder-by-feature approach, where code is organized into package folders by feature rather than by class type. Packaging classes by feature provides a clear view of the "components" required to support the feature. Utilizing this organization method also allows for easier modularization when a feature is desirable in other apps. 

#### Example
/com/company/app
   /search
      SearchController
      SearchService
      SearchRepository
      SearchCriteria
   /security
      UserController
      UserService
      UserRepository
      User

#### Notes
* If you are adding a new feature, create a new folder. 
* If you are modifying or adding to an existing feature, then work within that feature's folder. 
* Be mindful of creating dependencies on other features. Too many depedencies on other feature may be a 'bad code smell' for your feature. 
* Avoid putting features or utilities in a 'common' package if possible. ONLY put features into common if they are TRULY generic and non-feature specific. Otherwise, just make a dependency on the feature you need. 
* Keep your folder structure as flat as possible, only build sub folders for sub features if the file count grows larger than ~7.

:arrow_up: [Back to Top](#table-of-contents)


### Run Tests

#### Command Line
1. cd /path/to/my/workspace/voyage-api-java
2. gradle test

#### IntelliJ
1. Within the Project sidebar pane, right-click on the `/src/test` folder  
   ![Test in IntelliJ](./images/RECIPES_RunTests1.png)

2. Choose the 'Run All Tests' option to run all unit and integrations tests found
   ![Test in IntelliJ 2](./images/RECIPES_RunTests2.png)

3. Run a specific test by right-clicking on the Test class and selecting 'Run >ClassNameSpec<'
   ![Test in IntelliJ 3](./images/RECIPES_RunTests3.png)

4. Alternatively, open the Gradle tool menu in IntelliJ and double click the 'verification > test' option
   ![Test in IntelliJ 4](./images/RECIPES_RunTests4.png)

## Web Services

### API Versioning
API versioning is handled through URL versioning. See the [Web Service Pattern for API Versioning](WEB-SERVICE-PATTERNS.md#versioning). 

Each version of the an api will have a new controller source file and a unique url that contains the version. The routing for these versions is handled via attributes. The steps for creating a new version of an API are roughly as follows:

1. If a subfolder does not exist for a version, create it 
   - \v1, \v2, \v3...
2. Add a route prefix to the static RoutePrefixes class
3. Create the a new controller
4. Add the RoutePrefix attribute at the class level
5. Add the Route attribute to each operation, specifying the route template

:arrow_up: [Back to Top](#table-of-contents)

### Consuming API Services
There are two type of services available via the API: anonymous and secure services. Anonymous services are those that can be used without any authentication. Meanwhile, secure services are those that require a bearer token in order to process the request.

#### Anonymous Services
Anonymous services can be used without including an Authorization header. The following endpoints are anonymous:

| Path | Description | 
|:----|:----|
| /api/vX/login | Attempts to authentication the user and returns an authorization token on success.|
| /api/vX/account/register | Registers a new user|
| /api/vX/hello | Returns application status information |

Each of the services can be used by issuing a standard HTTP Request. For more information, see the API documentation for details on consuming these services.

##### Sample Anonymous Request

```
GET http://54.196.167.24/api/v1/hello HTTP/1.1
Host: 54.196.167.24
Connection: keep-alive
Cache-Control: no-cache
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36
Postman-Token: 554afee4-3092-c84e-abb6-5a9467a38c52
Accept: */*
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

##### Sample Anonymous Response

```
HTTP/1.1 200 OK
Content-Length: 29
Content-Type: application/json; charset=utf-8
Server: Microsoft-IIS/7.5
X-Powered-By: ASP.NET
Date: Tue, 06 Dec 2016 21:43:57 GMT

{"status": "alive", "datetime": "2016-12-23 17:55:55 UTC"}
```

#### Secure Services
Secure services need to have a special header included in the request. The header is:

```
{
    "Authorization": "bearer {Token}"
}
```
where {Token} is the access_token returned from the login service.

The workflow for consuming these sevices is as follows:

1. Call the Login api with the username and password
2. Upon a 200 response, save the access_token
3. Create a new request and set the Authorization header to bearer + access_token
4. Configure remaining properties of the request
5. Execute the request

**Note:** The access_token can be reused for multiple requests. It is unnecessary to call Login prior to every secure request. 

##### Sample Login Request

```
POST http://54.196.167.24/api/v1/login HTTP/1.1
Host: 54.196.167.24
Connection: keep-alive
Content-Length: 65
Postman-Token: 63193519-f0b2-b6eb-b905-939487bc103f
Cache-Control: no-cache
Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36
Content-Type: application/x-www-form-urlencoded
Accept: */*
Accept-Encoding: gzip, deflate
Accept-Language: en-US,en;q=0.8

grant_type=password&username=admin%40admin.com&password=Hello123!

```

##### Sample Login Response

```
HTTP/1.1 200 OK
Cache-Control: no-cache
Pragma: no-cache
Content-Length: 921
Content-Type: application/json;charset=UTF-8
Expires: -1
Server: Microsoft-IIS/7.5
Access-Control-Allow-Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop
Access-Control-Allow-Credentials: true
X-Powered-By: ASP.NET
Date: Tue, 06 Dec 2016 21:47:04 GMT

{"access_token":"eSqrWlnTcxdMBEB_MO-i812xCBysFGidFy2KHsYjRDbP21vBT5XpuHd3f_fxAYMvyuDhC84S02oAAzd8y4JO07R-R4svWnMiF38EOrJIpKJeP9S-aEs9TVpq7LQEJ1fZlSVcDZV_xttBTetveordQl0vKF7021fXCu05N-X4Y_SIQmVliiwk3v4xSx8skrobV4HBEDyiPEjYut04l_9j_m9BiEzfuGp0_B_o8phUu29SLRMhbtCrGHtCxrfW0BqcsRE3eerp2w2U-ynalVAWgTH339CmWFRK44WPgfpTUVNwnKnj2mr40iglYqKCi-ifxlA_9F4dNfJ4ixQj4QpIjXkAV9_WB1bsCnl-0cVsbWAvmHtIrXQKy9LKt7KykncxuCQlOMgt0K8BH-T9kSffiU2xpnBa9pcYsAIVN1ObZpkjV9RhyVwEyNphIEpUqHwir_fRMBtuSiH1gG5v3H60Vdr_cROTui9x-fhizM-s3ZnlviQhHc1qbhAIA48zYm8GOO8XO_z2H8zS-t94NVthBd1Z3QtI-024HmmXd9NzRKg0A30xSQsE0URm7-2jUN1HUwVNbNuDjXEf05FR2yfGnxoIWA5ZP25ozy_3UBsFXXWAiEDumi6Kk57M2B-xoINzxfZIJvcd_H_9EObGG8G_bcyAXHKnbu0Gwrbhb3Zwv4fslPI1diMXox4kJZtYxEOTjCSaZQlM9sq7I64Fl6apzj_PKLtRw1JMOgKLLzF9Ceo","token_type":"bearer","expires_in":86399,"userName":"admin@admin.com",".issued":"Tue, 06 Dec 2016 21:46:56 GMT",".expires":"Wed, 07 Dec 2016 21:46:56 GMT"}

```

##### Sample Secure Request

```
GET http://54.196.167.24/api/v1/roles HTTP/1.1
Host: 54.196.167.24
Connection: keep-alive
Authorization: bearer eSqrWlnTcxdMBEB_MO-i812xCBysFGidFy2KHsYjRDbP21vBT5XpuHd3f_fxAYMvyuDhC84S02oAAzd8y4JO07R-R4svWnMiF38EOrJIpKJeP9S-aEs9TVpq7LQEJ1fZlSVcDZV_xttBTetveordQl0vKF7021fXCu05N-X4Y_SIQmVliiwk3v4xSx8skrobV4HBEDyiPEjYut04l_9j_m9BiEzfuGp0_B_o8phUu29SLRMhbtCrGHtCxrfW0BqcsRE3eerp2w2U-ynalVAWgTH339CmWFRK44WPgfpTUVNwnKnj2mr40iglYqKCi-ifxlA_9F4dNfJ4ixQj4QpIjXkAV9_WB1bsCnl-0cVsbWAvmHtIrXQKy9LKt7KykncxuCQlOMgt0K8BH-T9kSffiU2xpnBa9pcYsAIVN1ObZpkjV9RhyVwEyNphIEpUqHwir_fRMBtuSiH1gG5v3H60Vdr_cROTui9x-fhizM-s3ZnlviQhHc1qbhAIA48zYm8GOO8XO_z2H8zS-t94NVthBd1Z3QtI-024HmmXd9NzRKg0A30xSQsE0URm7-2jUN1HUwVNbNuDjXEf05FR2yfGnxoIWA5ZP25ozy_3UBsFXXWAiEDumi6Kk57M2B-xoINzxfZIJvcd_H_9EObGG8G_bcyAXHKnbu0Gwrbhb3Zwv4fslPI1diMXox4kJZtYxEOTjCSaZQlM9sq7I64Fl6apzj_PKLtRw1JMOgKLLzF9Ceo
Cache-Control: no-cache
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36
Postman-Token: 28d8eeb2-8d05-c248-0b68-66589d8f7088
Accept: */*
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

##### Sample Secure Response

```
HTTP/1.1 200 OK
Content-Length: 1751
Content-Type: application/json; charset=utf-8
Server: Microsoft-IIS/7.5
X-Powered-By: ASP.NET
Date: Tue, 06 Dec 2016 21:47:50 GMT

[{"id":"3a408ee9-e99d-4ca3-bc5d-a1dec54d6d85","name":"Administrator","claims":[{"claimType":"lss.permission","claimValue":"assign.role","id":3},{"claimType":"lss.permission","claimValue":"create.role","id":4},{"claimType":"lss.permission","claimValue":"delete.role","id":5},{"claimType":"lss.permission","claimValue":"list.roles","id":6},{"claimType":"lss.permission","claimValue":"revoke.role","id":7},{"claimType":"lss.permission","claimValue":"view.claim","id":8},{"claimType":"lss.permission","claimValue":"list.users","id":9},{"claimType":"lss.permission","claimValue":"list.user-claims","id":10},{"claimType":"lss.permission","claimValue":"view.user","id":11},{"claimType":"lss.permission","claimValue":"update.user","id":12},{"claimType":"lss.permission","claimValue":"delete.user","id":13},{"claimType":"lss.permission","claimValue":"create.user","id":14},{"claimType":"lss.permission","claimValue":"list.widgets","id":15},{"claimType":"lss.permission","claimValue":"list.role-claims","id":16},{"claimType":"lss.permission","claimValue":"delete.role-claim","id":17},{"claimType":"lss.permission","claimValue":"create.claim","id":18},{"claimType":"lss.permission","claimValue":"view.role","id":19},{"claimType":"lss.permission","claimValue":"view.widget","id":20},{"claimType":"lss.permission","claimValue":"update.widget","id":21},{"claimType":"lss.permission","claimValue":"create.widget","id":22},{"claimType":"lss.permission","claimValue":"delete.widget","id":23}]},{"id":"1194fe58-7581-4273-badf-be2a29b939a3","name":"Basic","claims":[{"claimType":"lss.permission","claimValue":"login","id":1},{"claimType":"lss.permission","claimValue":"list.user-claims","id":2}]},{"id":"d090fa99-b987-412d-aad3-6c0090569098","name":"Super","claims":[]}]
```

:arrow_up: [Back to Top](#table-of-contents)


### Create a Web Service Endpoint Controller

#### Overview
Creating a controller will expose a new API endpoint. Controllers should be concerned with the API endpoint route and returning an appropriate HttpStatusCode. They should depend on services to execute to the business logic and return an object that represents that result that should be passed to the client.

#### Annotations
When adding a new controller, there are several annotations that should be used to decorate the class. The table below describes these attributes.

| Annotation  | Example | Scope | Description |
|:----|:----|:----|:----|
|RestController|`@RestController`|Class|Any API controller should have this attribute.|
|RequestMapping|`@RequestMapping(['/api/v1/users'])`|Class|Sets the base URL for the controller itself, multiple base URLs can be set in array format for versioning|
|HTTPMapping|`@GetMapping('/{id}')`|Method|Specifies the HTTP type (Get, Post, Put, Delete) of the endpoint and the sub-route.|
|PreAuthorize|`@PreAuthorize("hasAuthority('api.users.get')")`|Method|For secured endpoint, defines what authority a user's role must contain to be able to access this endpoint.|
|RequestBody|`save(@RequestBody User user)`|Parameter|Binds parameter to the request body for PUT and POST endpoints.|
|PathVariable|`get(@PathVariable('id') long id)`|Parameter|Binds parameter to matching value in the URL.|

For example, a simple CRUD user management controller looks like the following (apiDoc documentation omitted for sake of the example):

```
@RestController
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    private final UserService userService

    @Autowired
    UserController(UserService userService) {
        this.userService = userService
    }

    @GetMapping
    @PreAuthorize("hasAuthority('api.users.list')")
    ResponseEntity list() {
        Iterable<User> users = userService.listAll()
        return new ResponseEntity(users, HttpStatus.OK)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('api.users.create')")
    ResponseEntity save(@RequestBody User user) {
        User newUser = userService.saveDetached(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/api/v1/users/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        User userFromDB = userService.get(id)
        return new ResponseEntity(userFromDB, HttpStatus.OK)
    }

    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        userService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.update')")
    ResponseEntity update(@RequestBody User user) {
        User modifiedUser = userService.saveDetached(user)
        return new ResponseEntity(modifiedUser, HttpStatus.OK)
    }
}
```

#### Implementation
The following steps provide guidance around adding a new service

1. Add a new class file to Voyage.Web in the correct feature folder
   1. The class should end in the suffix Controller
3. Add class and method attributes
4. Inject services
5. Invoke the service and pass the result to the appropriate base method to generate the correct IHttpActionResult

Sample Method

```
@PutMapping('/{id}')
@PreAuthorize("hasAuthority('api.users.update')")
ResponseEntity update(@RequestBody User user) {
   User modifiedUser = userService.saveDetached(user)
   return new ResponseEntity(modifiedUser, HttpStatus.OK)
}
```
:arrow_up: [Back to Top](#table-of-contents)



### HTTP Request - Validate Request Data

Request data is validated using the @NotNull and @Valid annotations on service parameters. @Valid uses the annotations on the object's class definition.

#### Adding Validation

* Annotate your model with validation annotations

Example model

```
@Entity
@Audited
class User extends AuditableEntity {
    @NotNull
    String firstName

    @NotNull
    String lastName

    @NotNull
    String username

    @Email
    @NotNull
    String email

    @NotNull
    String password

    @NotNull
    Boolean isEnabled = Boolean.TRUE

    @NotNull
    Boolean isAccountExpired = Boolean.FALSE

    @NotNull
    Boolean isAccountLocked = Boolean.FALSE

    @NotNull
    Boolean isCredentialsExpired = Boolean.FALSE

    @NotNull
    @JsonIgnore
    Boolean isVerifyRequired = Boolean.FALSE

    @ManyToMany
    @JoinTable(name='user_role', joinColumns=@JoinColumn(name='user_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles
}
```

:arrow_up: [Back to Top](#table-of-contents)


### Securing a Web Service endpoint

#### Overview
[Spring Security](https://projects.spring.io/spring-security/) is implemented within this API to control access to any area of the app. Spring Security has a two primary concepts: Authentication, Authorization. Authentication is the act of recognizing a User, usually from a username and password. Authorization is the act of validating the permissions available to the user within the applicaiton. 

Securing a web service endpoint within a Controller relates to Authorization. By the time Spring Security allows for the Controller method to be invoked, the developer can assume that there already exists a User object within the [SpringSecurityContext](http://docs.spring.io/spring-security/site/docs/3.0.x/reference/technical-overview.html). 

The stock Spring Security implementation comes with [Role-based Authorization](http://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html), which assigns User's to roles and Roles are hardcoded into the source code to control access. The default Role based security pattern is not extensible without making source code changes. If a new role needs to be introduced, for example 'Manager 2', then developers must go into every area of the application, add the new role to the source code, and go through the entire deployment process to production. In some enterprise environments, this can be many weeks/months before the role can be accessible to the user. Because of this limitation, Voyage extends Spring Security and provides it's own `UserDetailsService` implemention (`/src/main/voyage/security/PermissionBasedUserDetailsService.groovy`) which introduces the concept of Permissions. Users have Roles, and Roles have Permissions. Permissions are hardcoded into the source code with names that are specific to that source code class or method. There shouldn't be a need to ever rename a permission once it's added to the source code, but Roles can change out permissions at any time by updating the role_permission relationship in the database. 

#### Secure Web Services
Secure SpringMVC Controller methods using the [@PreAuthorize Spring Security Annotation](http://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html). There are other annotations that can be used, but they will not be discussed in this section (ie @PreAuthorize, @PreFilter, @PostAuthorize and @PostFilter). 

```
@RestController
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    private final UserService userService

    @Autowired
    UserController(UserService userService) {
        this.userService = userService
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('api.users.list')")
    ResponseEntity list() {
        Iterable<User> users = userService.listAll()
        return new ResponseEntity(users, HttpStatus.OK)
    }
}
```
* @PreAuthorize("hasAuthority('api.users.list')") is added above the method signature of the list() method
* @PreAuthorize uses [Spring Expression Language (SpEL)](http://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html) inside the annotation to define the criteria for pre-authorization. 
* hasAuthority(..) SpEL method fetches the current User and checks to see if the given 'api.users.list' string matches any of the User's permissions.
  - If the current User has the permission, then the list() method is executed
  - If the current User does NOT have the permission, then a 401 Unauthorized HTTP response will be returned.
* The `/src/main/voyage/security/PermissionBasedUserDetailsService.groovy` class defines how Spring Security fetches the user and the User's permissions for analysis by the hasAuthority() method. 
* __NOTE:__ Controller methods *without* a security annotation, like @PreAuthorize, will be open to the public. Be sure to secure methods that need it (which should be all of them in most cases)!

#### Instructions
1. Create a new web service endpoint method in a Controller. This example creates a publicly accessible `list()` method in the ThingController.
   ```
   @RestController
   @RequestMapping(['/api/v1/things'])
   class ThingController {

       @GetMapping
       ResponseEntity list() {
           List<Thing> things = [new Thing()]
           return new ResponseEntity(things, HttpStatus.OK)
       }
   }
   ```
2. Add the @PreAuthorize annotation above the `list()` method signature to add a security check.
   - Define a unique permission name: api.things.list
   - Check the Permission table within the Voyage database to verify that this permission name is not already taken.
   ```
   @RestController
   @RequestMapping(['/api/v1/things'])
   class ThingController {

       @GetMapping
       @PreAuthorize("hasAuthority('api.things.list')")
       ResponseEntity list() {
           List<Thing> things = [new Thing()]
           return new ResponseEntity(things, HttpStatus.OK)
       }
   }
   ```
3. Add the new permission name to the database
   - Create a new Liquibase migration changelog file for the Permission change, or extend an existing Permission migration file for the current version of the app.
   - Add a new changeSet block to the migration file
     - Give the changeSet a unique ID and enter your name for the author
     - Copy an existing permission changeSet from `/src/main/resources/db.changelog/v1-0/permission.yaml` and tweak as needed
   - NOTE: Set permission.is_immutable to true or false depending on if you do not want the the permission to be modified through the API. There is a set of web services that allow for permissions to be added, updated, and deleted. Setting is_immutable to 'true' will ensure that the permissions API is not used on this permission. 
   ```
   databaseChangeLog:
     - changeSet:
      id: v1.0-permission-seed-things-list-api
      author: Voyage
      changes:
        - insert:
            tableName: permission
            columns:
              - column:
                  name: name
                  value: api.things.list
              - column:
                  name: description
                  value: /things GET web service endpoint to return a full list of things
              - column:
                  name: is_immutable
                  valueBoolean: true
              - column:
                  name: created_by
                  value: super
              - column:
                  name: created_date
                  valueDate: ${now}
              - column:
                  name: last_modified_by
                  value: super
              - column:
                  name: last_modified_date
                  valueDate: ${now}
              - column:
                  name: is_deleted
                  valueBoolean: false
   ```
4. Assign the new permission to one or more Roles
   - Create a new Liquibase migration changelog file for the Role addition, or extend an existing Role migration file for the current version of the app.
   - Add a new changeSet block to the migration file
     - Give the chlkangeSet a unique ID and enter your name for the author
     - Copy an existing role_permission changeSet from `/src/main/resources/db.changelog/v1-0/role_permission.yaml` and tweak as needed 
   - NOTE: You will need to lookup the Role ID and the Permission ID from the database in one of two ways:
     1. Run the migration scripts without the role_permission changeSet and look up the Role ID and Permission ID in a local database.
     2. Leverage functions within Liquibase framework to dynamically look up the Role ID and Permission ID by name. This is out of scope for this developer recipe. 
   ```
   databaseChangeLog:
     - changeSet:
      id: v1.0-role-permission-things-list-api
      author: Voyage
      changes:
        - insert:
            tableName: role_permission
            columns:
              - column:
                  name: role_id
                  value: 1
              - column:
                  name: permission_id
                  value: 15
   ```
5. Test the security of the web service
   1. Start up the app and make sure all of the migration scripts run successfull
   2. Inspect your local database to make sure that the data for permission and role_permission are correctly inserted
   3. Authenticate to the web service API with a user that DOESN'T belong to the Role with the new permission (see [Development > Accessing Secure Web Services](./DEVELOPMENT.md#access-secured-web-services))
      - There are 2 default users within the Voyage seed database: super, standard
      - By default, 'super' user will have all permissions
      - Test your web service security with the 'standard' user first, assuming 'standard' doesn't belong to a Role that has the new Permission. 
   4. Verify that Spring Security returns a 401 Unauthorized when accessing the web service endpoint `http://localhost:8080/api/v1/things`
   5. Authenticate to the web service API with a user that DOES belong to the Role with the new permission
   6. Verify that the expected JSON response is returned when accessing the web service `http://localhost:8080/api/v1/things`



## Service & Domain Logic

### Creating a Service
Services perform all business logic in the application.

#### Annotations

| Annotation  | Example | Scope | Description |
|:----|:----|:----|:----|
|Transactional|`@Transactional`|Class|Makes the service's sql calls transactional|
|Service|`@Service`|Class|Sets the base URL for the controller itself, multiple base URLs can be set in array format for versioning|
|Validated|`@Validated`|Method|Specifies the HTTP type (Get, Post, Put, Delete) of the endpoint and the sub-route.|
|NotNull|`User get(@NotNull Long id)`|Parameter|Validates that the parameter passed is not null|
|Valid|`User saveDetached(@Valid User userIn)`|Parameter|Validates the object passed in based on annotations in the class definition|

#### Implementation
The following steps provide guidance around adding a new service

1. Add a new class to your feature folder
  1. This file should end in the suffix Service to indicate it is a service
2. Declare service methods

Sample Service

```
@Service
@Transactional
@Validated
class ClientService {
    private final ClientRepository clientRepository

    ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository
    }

    Client getCurrentClient() {
        String clientId
        Authentication authentication = SecurityContextHolder.context.authentication
        if (authentication && authentication instanceof OAuth2Authentication) {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication
            clientId = oAuth2Authentication.OAuth2Request.clientId
        }

        if (clientId) {
            return findByClientIdentifier(clientId)
        }

        return null
    }

    Client findByClientIdentifier(@NotNull String clientIdentifier) {
        return clientRepository.findByClientIdentifier(clientIdentifier)
    }

    Client save(@Valid Client client) {
        clientRepository.save(client)
    }
}

```

:arrow_up: [Back to Top](#table-of-contents)



## Build & Deploy

### Clean workspace and rebuild
#### Command line
`voyage-api-java > gradle clean build`

#### IntelliJ
1. Rebuild the IntelliJ compiled source files in /out folder by clicking the file menu 'Build' and option 'Rebuild Project'
![IntelliJ Build & Deploy 1](./images/RECIPES_CleanAndBuild1.png)

2. Rebuild by invoking the Gradle 'clean' and 'build' tasks within the Gradle tool window
![IntelliJ Build & Deploy 2](./images/RECIPES_CleanAndBuild2.png)

:arrow_up: [Back to Top](#table-of-contents)

## Data Layer

### Create a database access Repository

#### Quick Refresher
1. Read up on Java Persistence API (JPA) to familiarize yourself with the annotation based decoration of domain objects to map to a relational database. 
   - [Hibernate Overview](http://hibernate.org/orm/)
   - [What's the difference between JPA & Hibernate](http://stackoverflow.com/questions/9881611/whats-the-difference-between-jpa-and-hibernate)
2. Read up on Spring Data JPA to familiarize yourself with how repository implementations are created from interfaces
   - [Getting Started: Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
   - [Working with Spring Data Repositories](http://docs.spring.io/spring-data/data-commons/docs/1.6.1.RELEASE/reference/html/repositories.html)
   
#### Create a domain object
Create a domain object that represents a database table. This domain object will be used to map to a database table row. 
```
@Entity
class User {
    @NotNull
    String firstName

    @NotNull
    String lastName
} 
```

* @Entity - the main annotation that signals to JPA/Hibernate that this object represents an entity in the database
* @NotNull - a constraint annotation that enforces that this field cannot be null when it is persisted to the database
* There are number of other JPA annotation for mapping relationships between @Entity domain objects. Read the full JPA / Hibernate specification for more details

#### Create a Repository interface
Spring Data JPA will automatically generate an implementation of the interface at runtime. Simply create the interface and provide additional annotations if necessary to instruct the implementation on how to behave. 
```
interface UserRepository extends CrudRepository<User, Long> {
    @Query('FROM User u WHERE u.username = ?1 AND u.isDeleted = false')
    User findByUsername(String username)

    @Query('FROM User u WHERE u.id = ?1 AND u.isDeleted = false')
    User findOne(Long id)

    @Query('FROM User u WHERE u.isDeleted = false')
    Iterable<User> findAll()
}
```
* interface - no need to create an actual implementation as Spring JPA will do this for you automatically at runtime
* `extends CrudRepository` - There are a number of interfaces that can be extended to provide features based on common patterns, such as the Create-Read-Update-Delete pattern. 
   - [CrudRepository JavaDoc](http://docs.spring.io/autorepo/docs/spring-data-commons/1.9.1.RELEASE/api/org/springframework/data/repository/CrudRepository.html)
* @Query - Annotate a method with @Query to specify the [JPQL](https://docs.jboss.org/hibernate/orm/4.3/devguide/en-US/html/ch11.html) that will be executed when the method is called. 
  - Note that the input parameters of the method are mapped into the query with a '?' followed by the input parameter order number. 
  - The 'username' input param for findByUsername(String username) will be mapped to '?1' in the Query [JPQL](https://docs.jboss.org/hibernate/orm/4.3/devguide/en-US/html/ch11.html)
* Create your own query using the method name convention described in [Working With Spring Repositories: Query Creation](http://docs.spring.io/spring-data/data-commons/docs/1.6.1.RELEASE/reference/html/repositories.html) section. 
* NOTE: findOne(Long id) is overridden from the CrudRepository interface because an additional criteria of 'u.isDeleted = false' needed to be added because logical deletes are implemented on the table. 

#### Create a Service method to interact with the Repository at runtime
The Repository interface is automatically implemented at runtime and placed into the Spring dependency container. The repository needs to be injected into a Service instance in order to be accessible to the application. 

```
@Service
class UserService {
    private final UserRepository userRepository
    
    @Autowired
    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    User findByUsername(@NotNull String username) {
        return userRepository.findByUsername(username)
    }

    User get(@NotNull Long id) {
        User user = userRepository.findOne(id)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        return user
    }
}
```

NOTE: ALWAYS call the Repository instances from a Service instance. Do not call Repository classes directly from Controllers or any other non-service instance. Always follow a 3-tier separation of concerns: 
 1. Controllers
 2. Services
 3. Repositories
 
Controllers shouldn't call Repositories and definitely not vice versa! The Service layer in the middle is there to hold all necessary business logic in the app and broker communications between the outside world (ie Controller) and the database (ie Repository). 

:arrow_up: [Back to Top](#table-of-contents)

### Add database structure changes
Database changes are managed within the app at start-up using the [Liquibase framework](http://www.liquibase.org). Liquibase employs a database migration strategy for deploying database changes, which means that changes are scripted and ordered into a progression of changes that are designed to bring the database to the latest structural state. 

#### Liquibase Notes
* Liqubase database migration scripts are stored in `/src/main/resources/db.changelog`
* Liquibase uses a table called DATABASECHANGELOG to track all changes that have been applied to the database
* Liquibase also generates a checksum on the migration script and will fail the app if an executed script checksum doesn't match the calculated checksum of the script in the filesystem. 
* All migration scripts follow the [YAML](http://www.yaml.org/start.html) file structure since it's less verbose than XML

#### Create a new liquibase migration file for your changes
1. Go to `/src/main/resources/db.changelog`
2. Go into the version sub-folder appropriate for your change OR create a new version folder if your changes are for a new version of the app. Example: `/src/main/resources/db.changelog/V1-0` 
3. Create a new YAML file for your changes named after the table name that is being changed
   - Exmample: user.yaml
   - NOTE: If the changes are not related to a specific table, then name the file based on the actions that will be performed, like 'add-indexes-to-tables.yaml'
   - Always use the 4 character extension for YAML files: .yaml
4. Add the new file to the `_db.changelog.yaml` file in the current version folder. 
   - If you are creating a new folder, then copy the `_db.changelog.yaml` file from another version folder as a template to modify for the new version folder. 
   - insert a new reference into the `_db.changelog.yaml` file that points to the new migration .yaml file.
   ```
   databaseChangeLog:
     - include:
         file: db/changelog/v1-0/action_log.yaml
     - include:
         file: db/changelog/v1-0/user.yaml
   ```

#### Add database changes to the migration script
1. Open the new migration file, like `/src/main/resources/db.changelog/V1-0/user.yaml`
2. Start the migration file with the opening root entry `databaseChangeLog:`
```
databaseChangeLog:
  - changeSet:
      id: v1.0-user-table-create
      author: Voyage
      changes:
        - createTable:
            tableName: user
            columns:
              - column:
                  name: id
                  type: int
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
```
3. Define each database within its own `- changeSet:` block (see above code block)
   - 'id' is a required attribute and must be defined for each changeSet. Use a descriptive id name as this will be printed in the console output and stored in the database. A readable and explanitory ID makes for easier debugging. 
   - 'author' is not required, but can be important for teams to identify who is responsible for the change
   - 'changes' is a grouping of one or more change commands.
4. Create liquibase change commands
   - Examine the existing liquibase scripts within /src/main/resources/db.changelog to find examples of how to do common database change tasks.
   - Read through the [Liquibase documentation](http://www.liquibase.org/documentation/index.html) to get a clear understanding on all of the available features
5. Manually test your changes in your local environment by running the app
   - `gradle bootRun`
   - Make sure that the database changes were applied via the console logs as well as within the database


:arrow_up: [Back to Top](#table-of-contents)

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
  - Securing a Web Service endpoint
* [Service & Domain logic](#service--domain-logic)
  - [Create a Service](#creating-a-service)
  - Error Handling w/ i18n support
* [Build & Deploy](#build-and-deploy)
  - Clean workspace and rebuild
* [Data Layer](#data-layer)
  - Create a database access Repository
  - Add database structure changes
  
  
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



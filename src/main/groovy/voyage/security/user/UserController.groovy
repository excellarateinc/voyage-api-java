package voyage.security.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    private final UserService userService

    @Autowired
    UserController(UserService userService) {
        this.userService = userService
    }

    /**
     * @api {get} /v1/users Get all users
     * @apiVersion 1.0.0
     * @apiName UserList
     * @apiGroup User
     *
     * @apiPermission api.user.list
     *
     * @apiUse AuthHeader
     *
     * @apiSuccess {Object[]} users List of users
     * @apiSuccess {String} users.id User ID
     * @apiSuccess {String} users.userName Username of the user
     * @apiSuccess {String} users.email Email
     * @apiSuccess {String} users.firstName First name
     * @apiSuccess {String} users.lastName Last name
     * @apiSuccess {Object[]} users.phones User phone numbers
     * @apiSuccess {String} users.phones.phoneNumber Phone number
     * @apiSuccess {String} users.phones.phoneType Phone type
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "id": "1",
     *           "userName": "admin",
     *           "email": "admin@admin.com",
     *           "firstName": "Admin_First",
     *           "lastName": "Admin_Last",
     *           "phones": [
     *              {"phoneNumber": "123-123-1233", "phoneType": "mobile"}
     *           ]
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.users.list')")
    ResponseEntity list() {
        Iterable<User> users = userService.listAll()
        return new ResponseEntity(users, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/users Create user
     * @apiVersion 1.0.0
     * @apiName UserCreate
     * @apiGroup User
     *
     * @apiPermission lss.permission->api.user.create
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/users/1"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.users.create')")
    ResponseEntity save(@RequestBody User user) {
        User newUser = userService.saveDetached(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/api/v1/users/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/users/:userId Get a user
     * @apiVersion 1.0.0
     * @apiName UserGet
     * @apiGroup User
     *
     * @apiPermission lss.permission->api.user.get
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} userId User ID
     *
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        User userFromDB = userService.get(id)
        return new ResponseEntity(userFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/users/:userId Delete a user
     * @apiVersion 1.0.0
     * @apiName UserDelete
     * @apiGroup User
     *
     * @apiPermission lss.permission->api.user.delete
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} userId User ID
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        userService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/users/:userId Update a user
     * @apiVersion 1.0.0
     * @apiName UserUpdate
     * @apiGroup User
     *
     * @apiPermission api.user.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.users.update')")
    ResponseEntity update(@RequestBody User user) {
        User modifiedUser = userService.saveDetached(user)
        return new ResponseEntity(modifiedUser, HttpStatus.OK)
    }
}

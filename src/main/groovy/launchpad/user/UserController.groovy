package launchpad.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(["/v1/users", "/v1.0/users"])
class UserController {
    private UserService userService

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
     * @apiPermission user.list
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
     *           "id": "A8DCF6EA-85A9-4D90-B722-3F4B9DE6642A",
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
    ResponseEntity list() {
        def users = userService.listAll()
        return new ResponseEntity(users, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/users Create user
     * @apiVersion 1.0.0
     * @apiName UserCreate
     * @apiGroup User
     *
     * @apiPermission lss.permission->create.user
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/users/b78ae241-1fa6-498c-aa48-9742245d0d2f"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    ResponseEntity save(@RequestBody User user) {
        user = userService.save(user)
        return new ResponseEntity(user, HttpStatus.OK)
    }

    /**
     * @api {get} /v1/users/:userId Get a user
     * @apiVersion 1.0.0
     * @apiName UserGet
     * @apiGroup User
     *
     * @apiPermission lss.permission->view.user
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} userId User ID
     *
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping("/{id}")
    ResponseEntity get(@PathVariable("id") long id) {
        def user = userService.get(id)
        return new ResponseEntity(user, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/users/:userId Delete a user
     * @apiVersion 1.0.0
     * @apiName UserDelete
     * @apiGroup User
     *
     * @apiPermission lss.permission->delete.user
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
    @DeleteMapping("/{id}")
    ResponseEntity delete(@PathVariable("id") long id) {
        userService.delete(id)
        return new ResponseEntity(HttpStatus.OK)
    }

    /**
     * @api {put} /v1/users/:userId Update a user
     * @apiVersion 1.0.0
     * @apiName UserUpdate
     * @apiGroup User
     *
     * @apiPermission user.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PutMapping("/{id}")
    ResponseEntity update(@RequestBody User user) {
        userService.update(user)
        return new ResponseEntity(user, HttpStatus.OK)
    }
}

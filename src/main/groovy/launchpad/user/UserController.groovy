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
     * @apiName GetUsers
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

    @PostMapping
    ResponseEntity save(@RequestBody User user) {
        user = userService.save(user)
        return new ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    ResponseEntity get(@PathVariable("id") long id) {
        def user = userService.get(id)
        return new ResponseEntity(user, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    ResponseEntity delete(@PathVariable("id") long id) {
        userService.delete(id)
        return new ResponseEntity(HttpStatus.OK)
    }

    @PutMapping("/{id}")
    ResponseEntity update(@RequestBody User user) {
        userService.update(user)
        return new ResponseEntity(user, HttpStatus.OK)
    }
}

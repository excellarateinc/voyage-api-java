package launchpad.account

import launchpad.security.user.User
import launchpad.security.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/v1/account', '/v1.0/account'])
class AccountController {
    private final UserService userService

    @Autowired
    AccountController(UserService userService) {
        this.userService = userService
    }

    /**
     * @api {post} /v1/account registration
     * @apiVersion 1.0.0
     * @apiName AccountCreate
     * @apiGroup Account
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/account/1"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    ResponseEntity save(@RequestBody User user) {
        User newUser = userService.save(user)
        userService.sendVerificationEmail(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }
}

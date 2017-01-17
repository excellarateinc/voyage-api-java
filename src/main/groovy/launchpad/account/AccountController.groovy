package launchpad.account

import launchpad.security.user.User
import launchpad.security.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    @PostMapping('/register')
    ResponseEntity register(@RequestBody Map<String, Object> userMap) {
        User newUser = userService.register(userMap)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    /**
     * @api {post} /v1/account registration
     * @apiVersion 1.0.0
     * @apiName AccountCreate
     * @apiGroup Account
     *
     * @apiPermission @apiPermission lss.permission->api.user.create
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/account/activate/verifyEmailCode"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PreAuthorize("hasAuthority('api.users.create')")
    @GetMapping('/activate/{verifyEmailCode}')
    ResponseEntity activate(@PathVariable('verifyEmailCode') String verifyEmailCode) {
        userService.activate(verifyEmailCode)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/resendActivationEmail Resend activation email
     * @apiVersion 1.0.0
     * @apiName Activation Email
     * @apiGroup Account
     *
     * @apiPermission @apiPermission lss.permission->api.user.create
     *
     * @apiUse AuthHeader
     *
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse BadRequestError
     **/
    @PreAuthorize("hasAuthority('api.users.create')")
    @GetMapping('/resendActivationEmail')
    ResponseEntity resendActivationEmail() {
        User user = userService.getLoggedInUser()
        userService.sendVerificationEmail(user)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/forgotPassword/:username Send password reset email
     * @apiVersion 1.0.0
     * @apiName Forgot password email
     * @apiGroup Account
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} username Username
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse BadRequestError
     **/
    @GetMapping('/forgotPassword/{username}')
    ResponseEntity forgotPassword(@PathVariable('username') String username) {
        User user = userService.findByUsername(username)
        userService.sendPasswordResetEmail(user)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/resetPassword/:resetPasswordCode Reset Password
     * @apiVersion 1.0.0
     * @apiName Account Password Reset
     * @apiGroup Account
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} resetPasswordCode Reset Password Code
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200
     *
     * @apiUse BadRequestError
     **/
    @PostMapping('/resetPassword/{resetPasswordCode}')
    ResponseEntity resetPassword(@PathVariable('resetPasswordCode') String resetPasswordCode, @RequestBody Map userMap) {
        //TODO: Use command object to validate password and confirm password
        userService.resetPassword(resetPasswordCode, userMap.password)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

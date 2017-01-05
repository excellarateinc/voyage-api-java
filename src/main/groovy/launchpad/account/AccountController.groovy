package launchpad.account

import launchpad.error.UnknownIdentifierException
import launchpad.security.token.Token
import launchpad.security.token.TokenService
import launchpad.security.user.User
import launchpad.security.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private final TokenService tokenService

    @Autowired
    AccountController(UserService userService, TokenService tokenService) {
        this.userService = userService
        this.tokenService = tokenService
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
    ResponseEntity register(@RequestBody User user) {
        user.isEnabled = false
        User newUser = userService.save(user)
        userService.sendVerificationEmail(user)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/account/activate/:token User activation
     * @apiVersion 1.0.0
     * @apiName AccountActivate
     * @apiGroup Account
     *
     * @apiPermission lss.permission->none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} token Verification Token
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse BadRequestError
     **/
    @GetMapping('/activate/{token}')
    ResponseEntity activate(@PathVariable('token') String token) {
        userService.activate(token)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/resendActivationEmail/:username Resend activation email
     * @apiVersion 1.0.0
     * @apiName Activation Email
     * @apiGroup Account
     *
     * @apiPermission lss.permission->none
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
    @GetMapping('/resendActivationEmail/{username}')
    ResponseEntity resendActivationEmail(@PathVariable('username') String username) {
        User user = userService.findByUsername(username)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        userService.sendVerificationEmail(user)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/resendActivationEmail/:username Send password reset email
     * @apiVersion 1.0.0
     * @apiName Forgot password email
     * @apiGroup Account
     *
     * @apiPermission lss.permission->none
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
        if (!user) {
            throw new UnknownIdentifierException()
        }
        userService.sendPasswordResetEmail(user)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {get} /v1/account/activate/:token Reset Password
     * @apiVersion 1.0.0
     * @apiName Account Password Reset
     * @apiGroup Account
     *
     * @apiPermission lss.permission->none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} token Verification Token
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse BadRequestError
     **/
    @GetMapping('/resetPassword/{token}')
    ResponseEntity resetPassword(@PathVariable('token') String tokenValue) {
        userService.validateUserByToken(tokenValue)
        return new ResponseEntity(tokenValue, HttpStatus.OK)
    }

    /**
     * @api {get} /v1/account/activate/:token Reset Password
     * @apiVersion 1.0.0
     * @apiName Account Password Reset
     * @apiGroup Account
     *
     * @apiPermission lss.permission->none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} token Verification Token
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200
     *
     * @apiUse BadRequestError
     **/
    @PostMapping('/resetPassword/{token}')
    ResponseEntity resetPassword(@PathVariable('token') String tokenValue, @RequestBody User user) {
        //TODO: Use command object to validate password and confirm password
        User userToUpdate = userService.findUserByToken(tokenValue)
        userToUpdate.password = user.password
        userService.save(userToUpdate)
        tokenService.expire(tokenValue)
        return new ResponseEntity(tokenValue, HttpStatus.OK)
    }
}

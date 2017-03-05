package voyage.account

import voyage.security.user.User
import voyage.security.user.UserVerifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/account', '/api/v1.0/account'])
class AccountController {
    private final AccountService accountService
    private final UserVerifyService userVerifyService

    @Autowired
    AccountController(AccountService accountService, UserVerifyService userVerifyService) {
        this.accountService = accountService
        this.userVerifyService = userVerifyService
    }

    /**
     * @api {post} /v1/register Create account
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
     *     "Location": "http://localhost:52431/api/v1/register/1"
     * }
     *
     * @apiUse UserRequestModel
     * @apiUse UserSuccessModel
     **/
    @PostMapping('/register')
    ResponseEntity register(@RequestBody User userIn) {
        User newUser = accountService.register(userIn)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/verify/methods Get Verify Methods
     * @apiVersion 1.0.0
     * @apiName VerifyMethodsList
     * @apiGroup Account
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiSuccess {Object[]} verifyMethods List of verification methods available for the user
     * @apiSuccess {String} verifyMethods.type The verification method type: email, text
     * @apiSuccess {String} verifyMethods.value The value associated with the method type (ie text 612-123-2221)
     * @apiSuccess {String} verifyMethods.label The verification method label (ie text)
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "label": "email",
     *           "verifyType": "email",
     *           "value": ""
     *       },
     *       {
     *           "label": "text",
     *           "verifyType": "text",
     *           "value": "1"
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @PreAuthorize('isAuthenticated()')
    @GetMapping('/verify/methods')
    ResponseEntity verifyMethods() {
        Iterable<VerifyMethod> verifyMethods = userVerifyService.verifyMethodsForCurrentUser
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/verify/send Send Verify Code
     * @apiVersion 1.0.0
     * @apiName VerifySend
     * @apiGroup Account
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify/send')
    ResponseEntity sendVerificationCode(@RequestBody VerifyMethod verifyMethod) {
        userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {post} /v1/verify Verify user
     * @apiVersion 1.0.0
     * @apiName VerifyAccount
     * @apiGroup Account
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify')
    ResponseEntity verify(@RequestBody String code) {
        userVerifyService.verifyCurrentUser(code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

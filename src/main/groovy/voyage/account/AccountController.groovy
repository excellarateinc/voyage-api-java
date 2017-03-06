package voyage.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import voyage.security.user.User
import voyage.security.verify.VerifyService

@RestController
@RequestMapping(['/api/v1/account', '/api/v1.0/account'])
class AccountController {
    private final AccountService accountService
    private final VerifyService userVerifyService

    @Autowired
    AccountController(AccountService accountService, VerifyService userVerifyService) {
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
}

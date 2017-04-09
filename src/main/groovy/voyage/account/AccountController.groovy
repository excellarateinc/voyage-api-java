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
     * @apiDescription Creates a new user account. All parameters are required and at least 1 mobile phone must be added.
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {Object} account Account
     * @apiParam {String} account.userName Username of the user
     * @apiParam {String} account.email Email
     * @apiParam {String} account.firstName First name
     * @apiParam {String} account.lastName Last name
     * @apiParam {String} account.password Password
     * @apiParam {Object[]} account.phones Account phone numbers
     * @apiParam {String} account.phones.phoneNumber Phone number in E.164 format (ie +16518886021 or +1-651-888-6021 as punctuation is stripped out)
     * @apiParam {String} account.phones.phoneType Phone type (mobile, office, home, other). NOTE: At least one mobile phone is required.
     *
     * @apiExample {json} Example body:
     * {
     *     "firstName": "FirstName",
     *     "lastName": "LastName",
     *     "username": "FirstName3@app.com",
     *     "email": "FirstName3@app.com",
     *     "password": "my-secure-password",
     *     "phones":
     *     [
     *         {
     *             "phoneType": "mobile",
     *             "phoneNumber" : "+6518886021"
     *         }
     *     ]
     * }
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} New Account Location
     * HTTP/1.1 201: Created
     * {
     *     "Location": "https://my-app/api/v1/account"
     * }
     *
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     **/
    @PostMapping()
    ResponseEntity register(@RequestBody User userIn) {
        accountService.register(userIn)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, '/v1/account')
        return new ResponseEntity(headers, HttpStatus.CREATED)
    }
}

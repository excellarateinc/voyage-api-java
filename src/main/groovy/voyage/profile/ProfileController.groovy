package voyage.profile

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
@RequestMapping(['/api/v1/profile', '/api/v1.0/profile'])
class ProfileController {
    private final ProfileService profileService
    private final VerifyService userVerifyService

    @Autowired
    ProfileController(ProfileService profileService, VerifyService userVerifyService) {
        this.profileService = profileService
        this.userVerifyService = userVerifyService
    }

    /**
     * @api {post} /v1/profile Create profile
     * @apiVersion 1.0.0
     * @apiName ProfileCreate
     * @apiGroup Profile
     *
     * @apiDescription Creates a new user profile. All parameters are required and at least 1 mobile phone must be added.
     *
     * @apiPermission none
     *
     * @apiUse AuthHeader
     *
     * @apiParam {Object} profile Profile
     * @apiParam {String} profile.userName Username of the user
     * @apiParam {String} profile.email Email
     * @apiParam {String} profile.firstName First name
     * @apiParam {String} profile.lastName Last name
     * @apiParam {String} profile.password ResetPassword
     * @apiParam {Object[]} profile.phones Profile phone numbers
     * @apiParam {String} profile.phones.phoneNumber Phone number in E.164 format (ie +16518886021 or +1-651-888-6021 as punctuation is stripped out)
     * @apiParam {String} profile.phones.phoneType Phone type (mobile, office, home, other). NOTE: At least one mobile phone is required.
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
     *             "phoneType": "MOBILE",
     *             "phoneNumber" : "+6518886021"
     *         }
     *     ]
     * }
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} New Profile Location
     * HTTP/1.1 201: Created
     * {
     *     "Location": "https://my-app/api/v1/profile"
     * }
     *
     * @apiUse UsernameAlreadyInUseError
     * @apiUse MobilePhoneNumberRequiredError
     **/
    @PostMapping()
    ResponseEntity save(@RequestBody User userIn) {
        profileService.save(userIn)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, '/v1/profile')
        return new ResponseEntity(headers, HttpStatus.CREATED)
    }

}

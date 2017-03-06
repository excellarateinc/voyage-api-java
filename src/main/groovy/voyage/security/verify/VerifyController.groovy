package voyage.security.verify

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/v1/verify', '/api/v1.0/verify'])
class VerifyController {
    private final VerifyService verifyService

    @Autowired
    VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService
    }

    /**
     * @api {get} /v1/verify/methods Get Verify Methods
     * @apiVersion 1.0.0
     * @apiName GetVerifyMethods
     * @apiGroup Verify
     *
     * @apiDescription Returns a list of e-mail and text message options that are available for the user to receive the verification code.
     * The 'verifyType' and 'value' response body attributes are to be used when invoking /v1/verify/send.
     *
     * ~ ~ ~ ~ VERIFY WORKFLOW ~ ~ ~ ~
     * 
     * The user verification process is a multi-step workflow that is required when a user has been flagged as needing
     * to assert that they are truly in control of their account. A user might be flagged for verification for any
     * reason at any time. The consumer of this API must be prepared to handle a user verification error response
     * (403 HTTP Status with errorCode 403_verify_user) for any API call made at any time.
     *
     * The verification process requires that the user authenticates with the API using their username and password,
     * and then enters a code that they receive through a pre-approved message channel like an e-mail or text message.
     * Once the user receives the verification code, then are instructed to enter the code into their app, which will
     * then call POST /verify web service to confirm the code with their user account. Once the valid code has been confirmed,
     * then the user can resume making their calls to the API. Until the user verification is completed, the user will
     * not be able to make any API calls other than the 'Verify' API web services.
     *
     * 1) User invokes an API (ie /account)
     *    * API replies with a 403 HTTP Status
     *    * Response body includes an error code of 403_verify_user
     * 2) User invokes GET /verify/methods
     *    * API replies with a listing of verification code delivery methods available for the user
     *    * A 'verifyMethod' and 'value' is provided with each method option
     * 3) User invokes POST /verify/send
     *    * API delivers verification code to the selected delivery method
     *    * Response includes a success status code
     * 4) User invokes POST /verify
     *    * API receives the verification code and validates the user
     *    * Response includes a success or failure status code with error message
     * 5) User invokes an API (ie /account)
     *    * Resumes accessing the API
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiSuccess {Object[]} verifyMethods List of verification methods available for the user
     * @apiSuccess {String} verifyMethods.label The partially obscured delivery method label recognizable to the user
     * @apiSuccess {String} verifyMethods.value The numeric value of the delivery method. Used by /verify/send API.
     * @apiSuccess {String} verifyMethods.verifyType The type of the verify method (EMAIL, TEXT). Used by the /verify/send API.
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "label": "email",
     *           "verifyType": "EMAIL",
     *           "value": "1"
     *       },
     *       {
     *           "label": "text",
     *           "verifyType": "TEXT",
     *           "value": "2"
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @PreAuthorize('isAuthenticated()')
    @GetMapping('/methods')
    ResponseEntity verifyMethods() {
        Iterable<VerifyMethod> verifyMethods = verifyService.verifyMethodsForCurrentUser
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/verify/send Send Verify Code
     * @apiVersion 1.0.0
     * @apiName PostVerifySend
     * @apiGroup Verify
     *
     * @apiDescription Sends a verification message to the given verification method specified in the body of this web
     * service request.
     *
     * ~ ~ ~ ~ VERIFY WORKFLOW ~ ~ ~ ~
     *
     * The user verification process is a multi-step workflow that is required when a user has been flagged as needing
     * to assert that they are truly in control of their account. A user might be flagged for verification for any
     * reason at any time. The consumer of this API must be prepared to handle a user verification error response
     * (403 HTTP Status with errorCode 403_verify_user) for any API call made at any time.
     *
     * The verification process requires that the user authenticates with the API using their username and password,
     * and then enters a code that they receive through a pre-approved message channel like an e-mail or text message.
     * Once the user receives the verification code, then are instructed to enter the code into their app, which will
     * then call POST /verify web service to confirm the code with their user account. Once the valid code has been confirmed,
     * then the user can resume making their calls to the API. Until the user verification is completed, the user will
     * not be able to make any API calls other than the 'Verify' API web services.
     *
     * 1) User invokes an API (ie /account)
     *    * API replies with a 403 HTTP Status
     *    * Response body includes an error code of 403_verify_user
     * 2) User invokes GET /verify/methods
     *    * API replies with a listing of verification code delivery methods available for the user
     *    * A 'verifyMethod' and 'value' is provided with each method option
     * 3) User invokes POST /verify/send
     *    * API delivers verification code to the selected delivery method
     *    * Response includes a success status code
     * 4) User invokes POST /verify
     *    * API receives the verification code and validates the user
     *    * Response includes a success or failure status code with error message
     * 5) User invokes an API (ie /account)
     *    * Resumes accessing the API
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiParam {Object} verifyType The verifyType provided in the /verify/methods response (ie EMAIL or TEXT)
     * @apiParam {String} value The verify method value number, which indicates which delivery method to use. See /verify/methods response.
     *
     * @apiExample {json} Example body:
     * {
     *     "verifyType": "TEXT",
     *     "value": 2
     * }
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     **/
    @PreAuthorize('isAuthenticated()')
    @PostMapping('/send')
    ResponseEntity sendVerificationCode(@RequestBody VerifyMethod verifyMethod) {
        verifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {post} /v1/verify Verify user
     * @apiVersion 1.0.0
     * @apiName PostVerify
     * @apiGroup Verify
     *
     * @apiDescription Validates the given verification code for the currently logged in user and returns a success or
     * failure message to the web service consumer. 
     *
     * ~ ~ ~ ~ VERIFY WORKFLOW ~ ~ ~ ~
     *
     * The user verification process is a multi-step workflow that is required when a user has been flagged as needing
     * to assert that they are truly in control of their account. A user might be flagged for verification for any
     * reason at any time. The consumer of this API must be prepared to handle a user verification error response
     * (403 HTTP Status with errorCode 403_verify_user) for any API call made at any time.
     *
     * The verification process requires that the user authenticates with the API using their username and password,
     * and then enters a code that they receive through a pre-approved message channel like an e-mail or text message.
     * Once the user receives the verification code, then are instructed to enter the code into their app, which will
     * then call POST /verify web service to confirm the code with their user account. Once the valid code has been confirmed,
     * then the user can resume making their calls to the API. Until the user verification is completed, the user will
     * not be able to make any API calls other than the 'Verify' API web services.
     *
     * 1) User invokes an API (ie /account)
     *    * API replies with a 403 HTTP Status
     *    * Response body includes an error code of 403_verify_user
     * 2) User invokes GET /verify/methods
     *    * API replies with a listing of verification code delivery methods available for the user
     *    * A 'verifyMethod' and 'value' is provided with each method option
     * 3) User invokes POST /verify/send
     *    * API delivers verification code to the selected delivery method
     *    * Response includes a success status code
     * 4) User invokes POST /verify
     *    * API receives the verification code and validates the user
     *    * Response includes a success or failure status code with error message
     * 5) User invokes an API (ie /account)
     *    * Resumes accessing the API
     *
     * @apiPermission authenticated
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} code The code that was delivered to user via the /verify/send method
     *
     * @apiExample {json} Example body:
     * {
     *     "code": "123456"
     * }
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     **/
    @PreAuthorize('isAuthenticated()')
    @PostMapping
    ResponseEntity verify(@RequestBody String code) {
        verifyService.verifyCurrentUser(code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

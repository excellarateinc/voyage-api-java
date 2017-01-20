package launchpad.account

import launchpad.security.user.User
import launchpad.security.user.UserService
import launchpad.security.user.VerifyCodeType
import launchpad.security.user.VerifyMethod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * todo : unit tests, api docs after finalizing the flow of verify account
 * forgot password and render proper response.
 */

@RestController
@RequestMapping(['/api/v1/account', '/api/v1.0/account'])
class AccountController {
    private final UserService userService

    @Autowired
    AccountController(UserService userService) {
        this.userService = userService
    }

    @PostMapping('/register')
    ResponseEntity register(@RequestBody Map<String, Object> userMap) {
        User newUser = userService.register(userMap)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    @PreAuthorize('isAuthenticated()')
    @GetMapping('/verify/initiate')
    ResponseEntity initiateVerification() {
        User user = userService.loggedInUser
        Iterable<VerifyMethod> verifyMethods = userService.getVerifyMethods(user)
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    @PreAuthorize('isAuthenticated()')
    @GetMapping('/verify/code')
    ResponseEntity getVerificationCode(@RequestParam('verify_method') String verifyMethod) {
        User user = userService.loggedInUser
        Iterable<User> users = userService.sendVerifyCode(user, verifyMethod, VerifyCodeType.ACCOUNT_VERIFICATION)
        return new ResponseEntity(users, HttpStatus.OK)
    }

    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify')
    ResponseEntity verify(@RequestBody String code) {
        User user = userService.loggedInUser
        userService.verify(code, user)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping('/recover/password')
    ResponseEntity initiatePasswordRecover(@RequestBody String username) {
        User user = userService.findByUsername(username)
        Iterable<VerifyMethod> verifyMethods = userService.getVerifyMethods(user)
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    @GetMapping('/recover/password/code')
    ResponseEntity getPasswordRecoverCode(@RequestParam('verify_method') String verifyMethod) {
        User user = userService.loggedInUser
        Iterable<User> users = userService.sendVerifyCode(user, verifyMethod, VerifyCodeType.PASSWORD_RESET)
        return new ResponseEntity(users, HttpStatus.OK)
    }

    @PostMapping('/recover/password/verify')
    ResponseEntity verifyPasswordRecoverCode(@RequestBody String code) {
        userService.verifyPasswordRecoverCode(code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping('/recover')
    ResponseEntity changePassword(@RequestBody Map<String, String> params) {
        userService.resetPassword(params.code, params.password)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

package launchpad.account

import launchpad.security.user.UserService
import launchpad.security.user.UserVerifyService
import launchpad.security.user.User
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
    private final AccountService accountService
    private final UserVerifyService userVerifyService
    private final UserService userService

    @Autowired
    AccountController(AccountService accountService, UserVerifyService userVerifyService, UserService userService) {
        this.accountService = accountService
        this.userVerifyService = userVerifyService
        this.userService = userService
    }

    @PostMapping('/register')
    ResponseEntity register(@RequestBody Map<String, Object> userMap) {
        User newUser = accountService.register(userMap)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/account/${newUser.id}")
        return new ResponseEntity(newUser, headers, HttpStatus.CREATED)
    }

    @PreAuthorize('isAuthenticated()')
    @GetMapping('/verify/methods')
    ResponseEntity verifyMethods() {
        Iterable<VerifyMethod> verifyMethods = userVerifyService.getVerifyMethodsForCurrentUser()
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    @PreAuthorize('isAuthenticated()')
    @GetMapping('/verify')
    ResponseEntity getVerificationCode(@RequestParam('verifyMethod') String verifyMethod) {
        userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify')
    ResponseEntity verify(@RequestBody String code) {
        userVerifyService.verifyCurrentUser(code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

package launchpad.account

import launchpad.security.user.UserVerifyService
import launchpad.security.user.User
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
import launchpad.error.ErrorResponse
import launchpad.error.ErrorUtils
import launchpad.error.PasswordRecoveryFailedException

/**
 * todo : unit tests, api docs after finalizing the flow of verify account
 * forgot password and render proper response.
 * reset password, logout, password policy rules yet to be implemented
 */

@RestController
@RequestMapping(['/api/v1/account', '/api/v1.0/account'])
class AccountController {
    private final AccountService accountService
    private final UserVerifyService userVerifyService
    private final SecurityQuestionService securityQuestionService
    private final UserSecurityQuestionService userSecurityQuestionService

    @Autowired
    AccountController(AccountService accountService, UserVerifyService userVerifyService,
                      SecurityQuestionService securityQuestionService,
                      UserSecurityQuestionService userSecurityQuestionService) {
        this.accountService = accountService
        this.userVerifyService = userVerifyService
        this.securityQuestionService = securityQuestionService
        this.userSecurityQuestionService = userSecurityQuestionService
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
        Iterable<VerifyMethod> verifyMethods = userVerifyService.verifyMethodsForCurrentUser
        return new ResponseEntity(verifyMethods, HttpStatus.OK)
    }

    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify/send')
    ResponseEntity sendVerificationCode(@RequestBody Map<String, Object> verifyMethodMap) {
        userVerifyService.sendVerifyCodeToCurrentUser(verifyMethodMap)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize('isAuthenticated()')
    @PostMapping('/verify')
    ResponseEntity verify(@RequestBody String code) {
        userVerifyService.verifyCurrentUser(code)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    //TODO Apidocs
    @GetMapping('/securityquestions')
    ResponseEntity listSecurityQuestions() {
        Iterable<SecurityQuestion> securityQuestions = securityQuestionService.listAll()
        return new ResponseEntity(securityQuestions, HttpStatus.OK)
    }

    //TODO Apidocs
    @PostMapping('/securityquestions')
    ResponseEntity saveUserSecurityAnswers(@RequestBody UserSecurityAnswers userSecurityAnswersIn) {
        userSecurityAnswersIn.securityAnswers.each { userSecurityAnswerIn ->
            UserSecurityQuestion userSecurityQuestion = new UserSecurityQuestion()
            User user = new User()
            user.id = userSecurityAnswersIn.userId
            SecurityQuestion securityQuestion = new SecurityQuestion()
            securityQuestion.id = userSecurityAnswerIn.questionId
            userSecurityQuestion.user = user
            userSecurityQuestion.question = securityQuestion
            userSecurityQuestion.answer = userSecurityAnswerIn.answer
            userSecurityQuestionService.saveOrUpdate(userSecurityQuestion)
        }
        return new ResponseEntity(HttpStatus.OK)
    }

    //TODO Apidocs
    @PostMapping('/recovery')
    ResponseEntity passwordRecovery(@RequestBody UserSecurityAnswers userSecurityAnswersIn) {
        try {
            if (userSecurityQuestionService.validateUserSecurityAnswers(userSecurityAnswersIn.userId, userSecurityAnswersIn.securityAnswers)) {
                //TODO Call OAUTH2 authentication server for temporary token
                return new ResponseEntity(HttpStatus.OK)
            }
            ErrorResponse errorResponse = new ErrorResponse(
                    error:ErrorUtils.getErrorCode(HttpStatus.BAD_REQUEST.value()),
                    errorDescription:'One or more answers are incorrect',)
            return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
        } catch (PasswordRecoveryFailedException ex) {
            ErrorResponse errorResponse = new ErrorResponse(
                    error:ErrorUtils.getErrorCode(HttpStatus.BAD_REQUEST.value()),
                    errorDescription:ex.message,)
            return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
        }
    }
}

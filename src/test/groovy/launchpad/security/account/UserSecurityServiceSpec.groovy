package launchpad.security.account

import launchpad.account.SecurityQuestion
import launchpad.account.UserSecurityQuestion
import launchpad.account.UserSecurityQuestionRepository
import launchpad.account.UserSecurityQuestionService
import launchpad.security.user.User
import launchpad.security.user.UserRepository
import spock.lang.Specification

//ToDo unit tests for methods - assertFailedAttemptRules validateUserSecurityAnswers performPostValidationUpdates are pending
class UserSecurityServiceSpec extends Specification {
    SecurityQuestion securityQuestion
    UserSecurityQuestion userSecurityQuestion
    UserSecurityQuestionRepository userSecurityQuestionRepository = Mock()
    UserRepository userRepository = Mock()
    UserSecurityQuestionService userSecurityQuestionService = new UserSecurityQuestionService(userSecurityQuestionRepository, userRepository)

    def setup() {
        User user = new User()
        user.setId(1)
        securityQuestion = new SecurityQuestion(question:'What is the name of your first pet')
        securityQuestion.setId(1)

        userSecurityQuestion = new UserSecurityQuestion()
        userSecurityQuestion.setAnswer('Lucky')
        userSecurityQuestion.user = user
        userSecurityQuestion.question = securityQuestion
    }

    def 'findSecurityQuestionsByUserId - returns a list of security questions for user'() {
        setup:
        userSecurityQuestionRepository.findByUserId(1) >> [userSecurityQuestion]
        when:
        Iterable<UserSecurityQuestion> userSecurityQuestionList = userSecurityQuestionService.findSecurityQuestionsByUserId(1)
        then:
        1 == userSecurityQuestionList.size()
    }

    def 'saveOrUpdate - inserts the security question if it does not already exist'() {
        setup:
        userSecurityQuestionRepository.save(_) >> userSecurityQuestion
        when:
        UserSecurityQuestion savedUserSecurityQuestion = userSecurityQuestionService.saveOrUpdate(userSecurityQuestion)
        then:
        '3nyyNjISf+SQptk0Twn5n1N6OhU2SpjjKXfi8oMiolo=' == savedUserSecurityQuestion.answer
        //answer is 1-way encrypted before saving to database
    }

    def 'get - calls the userSecurityQuestionRepository.findOne'() {
        setup:
        userSecurityQuestionRepository.findOne(1) >> userSecurityQuestion
        when:
        UserSecurityQuestion fetchedUserSecurityQuestion = userSecurityQuestionService.get(1)
        then:
        'Lucky' == fetchedUserSecurityQuestion.answer
        !fetchedUserSecurityQuestion.isDeleted
    }

    def 'delete - verifies the object and calls userSecurityQuestionRepository.delete'() {
        setup:
        userSecurityQuestionRepository.findOne(_) >> userSecurityQuestion
        when:
        userSecurityQuestionService.delete(1)
        then:
        userSecurityQuestion.isDeleted
    }

    def 'encryptAnswer - returns encrypted answer using one-way encryption algorithm'() {
        when:
        String encryptedAnswer = userSecurityQuestionService.encryptAnswer('Lucky')
        then:
        '3nyyNjISf+SQptk0Twn5n1N6OhU2SpjjKXfi8oMiolo=' == encryptedAnswer
    }
}

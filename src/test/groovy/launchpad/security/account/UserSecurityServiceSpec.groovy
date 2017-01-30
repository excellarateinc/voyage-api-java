package launchpad.security.account

import launchpad.account.*
import launchpad.security.user.User
import spock.lang.Specification

class UserSecurityServiceSpec extends Specification {
    SecurityQuestion securityQuestion
    UserSecurityAnswer userSecurityAnswer
    UserSecurityRepository userSecurityRepository = Mock()
    UserSecurityService userSecurityService = new UserSecurityService(userSecurityRepository)

    def setup() {
        User user = new User();
        user.setId(1)
        securityQuestion = new SecurityQuestion(question: 'What is the name of your first pet')

        userSecurityAnswer = new UserSecurityAnswer()
        userSecurityAnswer.setAnswer('Lucky')
        userSecurityAnswer.setUser_id(user)
        userSecurityAnswer.setQuestion_id(securityQuestion)
    }

    def 'getSecurityAnswersForUser - returns a single result' () {
        setup:
            userSecurityRepository.findByUserId(1) >> [userSecurityAnswer]
        when:
            Iterable<UserSecurityAnswer> userSecurityAnswerList = userSecurityService.findSecurityAnswersByUserId(1)
        then:
            1 == userSecurityAnswerList.size()
    }

    def 'addUserSecurityAnswer - inserts the security question if it does not already exist'() {
        setup:
            userSecurityRepository.save(_) >> userSecurityAnswer
        when:
            UserSecurityAnswer savedAnswer = userSecurityService.save(userSecurityAnswer)
        then:
            'Lucky' == savedAnswer.answer
    }

    def 'get - calls the userSecurityRepository.findOne' () {
        setup:
            userSecurityRepository.findOne(_) >> userSecurityAnswer
        when:
            UserSecurityAnswer fetchedUserSecurityAnswer = userSecurityService.get(1)
        then:
            'Lucky' == fetchedUserSecurityAnswer.answer
            !fetchedUserSecurityAnswer.isDeleted
    }

    def 'delete - verifies the object and calls userSecurityRepository.delete' () {
        setup:
            userSecurityRepository.findOne(_) >> userSecurityAnswer
        when:
            userSecurityService.delete(1)
        then:
            userSecurityAnswer.isDeleted
    }
}
package launchpad.security.account

import launchpad.account.SecurityQuestion
import launchpad.account.SecurityQuestionRepository
import launchpad.account.SecurityQuestionService
import spock.lang.Specification

class SecurityServiceSpec extends Specification {
    SecurityQuestion securityQuestion
    SecurityQuestionRepository securityQuestionRepository = Mock()
    SecurityQuestionService securityQuestionService = new SecurityQuestionService(securityQuestionRepository)

    def setup() {
        securityQuestion = new SecurityQuestion(question:'What is the name of your first pet')
    }

    def 'getSecurityQuestions - returns a single result'() {
        setup:
        securityQuestionRepository.findAll() >> [securityQuestion]
        when:
        List<SecurityQuestion> securityQuestionList = securityQuestionService.listAll()
        then:
        1 == securityQuestionList.size()
    }

    def 'addSecurityQuestion - inserts the security question if it does not already exist'() {
        setup:
        securityQuestionRepository.save(_) >> securityQuestion
        when:
        SecurityQuestion savedQuestion = securityQuestionService.saveOrUpdate(securityQuestion)
        then:
        'What is the name of your first pet' == savedQuestion.question
    }

    def 'get - calls the securityQuestionRepository.findOne'() {
        setup:
        securityQuestionRepository.findOne(1) >> securityQuestion
        when:
        SecurityQuestion fetchedSecurityQuestion = securityQuestionService.get(1)
        then:
        'What is the name of your first pet' == fetchedSecurityQuestion.question
    }

    def 'delete - verifies the object and calls securityQuestionRepository.delete'() {
        setup:
        securityQuestionRepository.findOne(1) >> securityQuestion
        when:
        securityQuestionService.delete(1)
        then:
        securityQuestion.isDeleted
    }
}


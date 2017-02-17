package launchpad.account

import launchpad.error.UnknownIdentifierException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Validated
class SecurityQuestionService {

    @Autowired
    private final SecurityQuestionRepository securityQuestionRepository

    SecurityQuestionService(SecurityQuestionRepository securityQuestionRepository) {
        this.securityQuestionRepository = securityQuestionRepository
    }

    List<SecurityQuestion> listAll() {
        return securityQuestionRepository.findAll()
    }

    void delete(@NotNull Long id) {
        SecurityQuestion securityQuestion = get(id)
        securityQuestion.isDeleted = true
        securityQuestionRepository.save(securityQuestion)
    }

    SecurityQuestion get(@NotNull Long id) {
        SecurityQuestion securityQuestion = securityQuestionRepository.findOne(id)
        if (!securityQuestion) {
            throw new UnknownIdentifierException('Could not find SecurityQuestion with id : ' + id)
        }
        return securityQuestion
    }

    SecurityQuestion saveOrUpdate(@Valid SecurityQuestion securityQuestionIn) {
        if (securityQuestionIn.id) {
            SecurityQuestion securityQuestion = get(securityQuestionIn.id)
            securityQuestion.with {
                question = securityQuestionIn.question
            }
            return securityQuestionRepository.save(securityQuestion)
        }
        return securityQuestionRepository.save(securityQuestionIn)
    }
}

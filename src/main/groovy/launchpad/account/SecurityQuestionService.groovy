package launchpad.account

import launchpad.error.UnknownIdentifierException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.util.List

@Service
@Transactional
@Validated
class SecurityQuestionService {

    @Autowired
    private final SecurityQuestionRepository securityQuestionRepository

    SecurityQuestionService(SecurityQuestionRepository securityQuestionRepository) {
        this.securityQuestionRepository = securityQuestionRepository
    }

    SecurityQuestion save(@Valid SecurityQuestion securityQuestion) {
        if (securityQuestion.id) {
            get(securityQuestion.id)
        }
        return securityQuestionRepository.save(securityQuestion)
    }

    List<SecurityQuestion> listAll(){
        return securityQuestionRepository.findAll();
    }

    void delete(@NotNull Long id) {
        SecurityQuestion securityQuestion = get(id)
        securityQuestion.isDeleted = true
        securityQuestionRepository.save(securityQuestion)
    }

    SecurityQuestion get(@NotNull Long id) {
        SecurityQuestion securityQuestion = securityQuestionRepository.findOne(id)
        if (!securityQuestion) {
            throw new UnknownIdentifierException()
        }
        return securityQuestion
    }
}

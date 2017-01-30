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
class UserSecurityService {

    @Autowired
    private final UserSecurityRepository userSecurityRepository

    UserSecurityService( UserSecurityRepository userSecurityRepository ) {
        this.userSecurityRepository = userSecurityRepository
    }

    UserSecurityAnswer save(@Valid UserSecurityAnswer userSecurityAnswer) {
        if (userSecurityAnswer.id) {
            get(userSecurityAnswer.id)
        }
        return userSecurityRepository.save(userSecurityAnswer)
    }

    List<UserSecurityAnswer> listAll(){
        return userSecurityRepository.findAll();
    }

    void delete(@NotNull Long id) {
        UserSecurityAnswer userSecurityAnswer = get(id)
        userSecurityAnswer.isDeleted = true
        userSecurityRepository.save(userSecurityAnswer)
    }

    UserSecurityAnswer get(@NotNull Long id) {
        UserSecurityAnswer userSecurityAnswer = userSecurityRepository.findOne(id)
        if (!userSecurityAnswer) {
            throw new UnknownIdentifierException()
        }
        return userSecurityAnswer
    }

    List<UserSecurityAnswer> findSecurityAnswersByUserId(@NotNull int user_id) {
        return userSecurityRepository.findByUserId(user_id)
    }
}

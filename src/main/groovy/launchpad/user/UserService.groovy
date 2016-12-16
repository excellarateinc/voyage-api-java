package launchpad.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Service class which provides the business methods to retrieve, create, update and delete User details.
 */
@Transactional
@Service("userService")
@Validated
class UserService {
    private final UserRepository userRepository

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    void delete(@NotNull Long id) {
        userRepository.delete(id)
    }

    User get(@NotNull Long id) {
        userRepository.findOne(id)
    }

    Iterable<User> listAll() {
        userRepository.findAll()
    }

    User save(@Valid User user) {
        userRepository.save(user)
    }

    User update(@Valid User user) {
        userRepository.save(user)
    }

}

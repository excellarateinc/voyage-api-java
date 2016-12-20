package launchpad.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service('userService')
@Validated
class UserService {
    private final UserRepository userRepository

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    void delete(@NotNull Long id) {
        userRepository.delete(id)
    }

    User findByUsername(@NotNull String username) {
        userRepository.findByUsername(username)
    }

    User get(@NotNull Long id) {
        return userRepository.findOne(id)
    }

    Iterable<User> listAll() {
        return userRepository.findAll()
    }

    User save(@Valid User user) {
        return userRepository.save(user)
    }

    User update(@Valid User user) {
        return userRepository.save(user)
    }

}

package launchpad.user

import launchpad.error.UnknownIdentifierException
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
        User user = get(id)
        user.isDeleted = true
        save(user)
    }

    User findByUsername(@NotNull String username) {
        userRepository.findByUsername(username)
    }

    User get(@NotNull Long id) {
        User user = userRepository.findOne(id)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        return user
    }

    Iterable<User> listAll() {
        return userRepository.findAll()
    }

    User save(@Valid User user) {
        if (user.id) {
            get(user.id)
        }
        return userRepository.save(user)
    }
}

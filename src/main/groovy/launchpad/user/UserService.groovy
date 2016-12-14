package launchpad.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service class which provides the business methods to retrieve, create, update and delete User details.
 */
@Transactional
@Service('userService')
class UserService {
    private final UserRepository userRepository

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    void delete(Long id) {
        userRepository.delete(id)
    }

    User get(Long id) {
        userRepository.findOne(id)
    }

    Iterable<User> listAll() {
        userRepository.findAll()
    }

    User save(User user) {
        userRepository.save(user)
    }

    User update(User user) {
        userRepository.save(user)
    }

}

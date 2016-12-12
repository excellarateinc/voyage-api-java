package launchpad.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service("userService")
class UserService {
    private UserRepository userRepository

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    void delete(Long id) {
        userRepository.delete(id)
    }

    User get(Long id) {
        return userRepository.findOne(id)
    }

    Iterable<User> listAll() {
        return userRepository.findAll()
    }

    User save(User user) {
        return userRepository.save(user)
    }

    User update(User user) {
        return userRepository.save(user)
    }

}

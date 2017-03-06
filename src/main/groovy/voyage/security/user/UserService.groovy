package voyage.security.user

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import voyage.error.UnknownIdentifierException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.error.UsernameAlreadyInUseException

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserService {
    private final UserRepository userRepository

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    static String getCurrentUsername() {
        String username = null
        Authentication authenticationToken = SecurityContextHolder.context.authentication
        if (authenticationToken?.principal instanceof UserDetails) {
            username = ((UserDetails)authenticationToken.principal).username
        } else if (authenticationToken?.principal instanceof String) {
            username = authenticationToken.principal
        }
        return username
    }

    User getCurrentUser() {
        String username = currentUsername
        if (username) {
            return findByUsername(username)
        }
        return null
    }

    void delete(@NotNull Long id) {
        User user = get(id)
        user.isDeleted = true
        userRepository.save(user)
    }

    User findByUsername(@NotNull String username) {
        return userRepository.findByUsername(username)
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

    User saveDetached(@Valid User userIn) {
        if (userIn.id) {
            User user = get(userIn.id)

            // Verify the Username
            if (user.username != userIn.username) {
                if (!isUsernameUnique(userIn.username)) {
                    throw new UsernameAlreadyInUseException()
                }
            }

            user.with {
                firstName = userIn.firstName
                lastName = userIn.lastName
                username = userIn.username
                email = userIn.email
                password = userIn.password
                isEnabled = userIn.isEnabled
                isAccountExpired = userIn.isAccountExpired
                isAccountLocked = userIn.isAccountLocked
                isCredentialsExpired = userIn.isCredentialsExpired
            }

            return userRepository.save(user)
        }

        // Verify the Username
        if (!isUsernameUnique(userIn.username)) {
            throw new UsernameAlreadyInUseException()
        }

        return userRepository.save(userIn)
    }

    boolean isUsernameUnique(String username, User user = null) {
        User matchingUser = userRepository.findByUsername(username)
        if (matchingUser == user) {
            return true
        }
        return matchingUser ? false : true
    }
}

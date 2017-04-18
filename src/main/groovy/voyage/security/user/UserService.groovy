package voyage.security.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.common.PhoneService
import voyage.common.error.UnknownIdentifierException
import voyage.security.crypto.CryptoService

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserService {
    private final UserRepository userRepository
    private final CryptoService cryptoService
    private final PhoneService phoneService

    @Autowired
    UserService(UserRepository userRepository, CryptoService cryptoService, PhoneService phoneService) {
        this.userRepository = userRepository
        this.cryptoService = cryptoService
        this.phoneService = phoneService
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
        User user

        if (userIn.id) {
            user = get(userIn.id)
        } else {
            user = new User()
        }

        if (userIn.username != user.username) {
            if (!isUsernameUnique(userIn.username)) {
                throw new UsernameAlreadyInUseException()
            }
        }

        user.with {
            firstName = userIn.firstName
            lastName = userIn.lastName
            username = userIn.username
            email = userIn.email
            isEnabled = userIn.isEnabled
            isAccountExpired = userIn.isAccountExpired
            isAccountLocked = userIn.isAccountLocked
            isCredentialsExpired = userIn.isCredentialsExpired

            // Default to true for new accounts
            isVerifyRequired = user.id ? userIn.isVerifyRequired : true
        }

        if (userIn.password != user.password) {
            user.password = cryptoService.hashEncode(userIn.password)
        }

        applyPhones(user, userIn)

        // Require at least one PhoneType.MOBILE phone
        UserPhone mobilePhone = user.phones?.find { it.phoneType == PhoneType.MOBILE && !it.isDeleted }
        if (!mobilePhone) {
            throw new MobilePhoneRequiredException()
        }

        return userRepository.save(user)
    }

    boolean isUsernameUnique(String username, User user = null) {
        User matchingUser = userRepository.findByUsername(username)
        if (matchingUser == user) {
            return true
        }
        return matchingUser ? false : true
    }

    private void applyPhones(User user, User userIn) {
        if (!userIn?.phones) {
            return
        }

        // Prevent an attacker from overloading the database will millions of phones. Used primarily for the profile
        // services that are exposed to anyone able to create and update an profile
        if (userIn.phones.size() > 5) {
            throw new TooManyPhonesException()
        }

        userIn.phones.each { phoneIn ->
            UserPhone userPhone = null
            if (phoneIn.id) {
                userPhone = user.phones?.find {
                    it.id == phoneIn.id && !it.isDeleted
                }
            }
            if (!userPhone) {
                userPhone = user.phones?.find {
                    it.phoneNumber == phoneIn.phoneNumber && !it.isDeleted
                }
            }
            if (!userPhone) {
                userPhone = new UserPhone()
                userPhone.user = user
                if (user.phones) {
                    user.phones.add(userPhone)
                } else {
                    user.phones = [userPhone]
                }
            }

            userPhone.phoneType = phoneIn.phoneType
            userPhone.phoneNumber = phoneService.toE164(phoneIn.phoneNumber)
        }

        Iterable<UserPhone> phonesToDelete = getPhonesToDelete(user.phones, userIn.phones)
        phonesToDelete.each { phone ->
            phone.isDeleted = true
        }
    }

    private static List<UserPhone> getPhonesToDelete(Iterable<UserPhone> currentPhones, Iterable<UserPhone> newPhones) {
        List toDelete = []
        currentPhones.each { currentPhone ->
            UserPhone phoneMatch = (UserPhone) newPhones.find { newPhone ->
                currentPhone.id == newPhone.id || currentPhone.phoneNumber == newPhone.phoneNumber
            }
            if (!phoneMatch) {
                toDelete.add(currentPhone)
            }
        }
        return toDelete
    }
}

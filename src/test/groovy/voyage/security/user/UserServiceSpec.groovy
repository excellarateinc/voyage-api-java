package voyage.security.user

import spock.lang.Specification

class UserServiceSpec extends Specification {
    User user
    User modifiedUser
    UserRepository userRepository = Mock()
    UserService userService = new UserService(userRepository)

    def setup() {
        user = new User(username:'username', firstName:'LSS', lastName:'India')
        modifiedUser = new User(username:'username', firstName:'LSS', lastName:'Inc')
    }

    def 'listAll - returns a single result' () {
        setup:
            userRepository.findAll() >> [user]
        when:
            Iterable<User> userList = userService.listAll()
        then:
            1 == userList.size()
    }

    def 'save - applies the values and calls the userRepository' () {
        setup:
            userRepository.save(_) >> user
        when:
            User savedUser = userService.saveDetached(user)
        then:
            'LSS' == savedUser.firstName
            'India' == savedUser.lastName
            !savedUser.isDeleted
    }

    def 'save - applies the values and calls the userRepository with UsernameAlreadyInUseException' () {
        setup:
            userRepository.findByUsername(_) >> user
        when:
            userService.saveDetached(user)
        then:
            thrown(UsernameAlreadyInUseException)
    }

    def 'save - applies the values and calls the userRepository with existing user and UsernameAlreadyInUseException' () {
        setup:
            User newUser = new User(id:1, username:'newusername', firstName:'LSS', lastName:'India')
            userRepository.findOne(_) >> user
            userRepository.findByUsername(_) >> user
        when:
            userService.saveDetached(newUser)
        then:
            thrown(UsernameAlreadyInUseException)
    }

    def 'get - calls the userRepository.findOne' () {
        setup:
            userRepository.findOne(_) >> user
        when:
            User fetchedUser = userService.get(1)
        then:
            'LSS' == fetchedUser.firstName
            'India' == fetchedUser.lastName
            !fetchedUser.isDeleted
    }

    def 'delete - verifies the object and calls userRepository.delete' () {
        setup:
            userRepository.findOne(_) >> user
        when:
            userService.delete(user.id)
        then:
            user.isDeleted
    }
}

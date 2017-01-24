package launchpad.security.user

import spock.lang.Specification

class UserServiceSpec extends Specification {
    User user
    User modifiedUser
    UserRepository userRepository = Mock()
    UserService userService = new UserService(userRepository)

    def setup() {
        user = new User(firstName:'LSS', lastName:'India')
        modifiedUser = new User(firstName:'LSS', lastName:'Inc')
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

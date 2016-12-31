package launchpad.security.user

import spock.lang.Specification

// TODO Rename the test method names to be similar to RoleServiceSpec
// TODO Add tests to verify user.isDeleted for each method
class UserServiceSpec extends Specification {
    User user
    User modifiedUser
    UserRepository userRepository = Mock()
    UserService userService = new UserService(userRepository)

    def setup() {
        user = new User(firstName:'LSS', lastName:'India')
        modifiedUser = new User(firstName:'LSS', lastName:'Inc')
    }

    def 'Test the list method of UserService' () {
        setup:
            userRepository.findAll() >> [user]
        when:
            Iterable<User> userList = userService.listAll()
        then:
            1 == userList.size()
    }

    def 'Test save method of UserService' () {
        setup:
            userRepository.save(_) >> user
        when:
            User savedUser = userService.save(user)
        then:
            'LSS' == savedUser.firstName
            'India' == savedUser.lastName
    }

    def 'Test update method of UserService' () {
        setup:
            userRepository.save(_) >> modifiedUser
        when:
            User modifiedUser = userService.save(user)
        then:
            'LSS' == modifiedUser.firstName
            'Inc' == modifiedUser.lastName
    }

    def 'Test find method of UserService' () {
        setup:
            userRepository.findOne(_) >> user
        when:
            User fetchedUser = userService.get(1)
        then:
            'LSS' == fetchedUser.firstName
            'India' == fetchedUser.lastName
    }

    def 'Test delete method of UserService' () {
        setup:
            userRepository.findOne(_) >> user
        when:
            userService.delete(user.id)
        then:
            user.isDeleted
    }
}

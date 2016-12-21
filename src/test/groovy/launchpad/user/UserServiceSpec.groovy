package launchpad.user

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

    def 'Test the list method of UserService' () {
        when:
            Iterable<User> userList = userService.listAll()
        then:
            1 * userRepository.findAll() >> [user]
            1 == userList.size()
    }

    def 'Test save method of UserService' () {
        when:
            User savedUser = userService.save(user)
        then:
            1 * userRepository.save({ User user -> user.firstName == 'LSS' }) >> modifiedUser
            'Inc' == savedUser.lastName
    }

    def 'Test update method of UserService' () {
        when:
            User modifiedUser = userService.save(user)
        then:
            1 * userRepository.save({ User user -> user.lastName == 'India' }) >> modifiedUser
            'Inc' == modifiedUser.lastName
    }

    def 'Test find method of UserService' () {
        when:
            User fetchedUser = userService.get(1)
        then:
            1 * userRepository.findOne(_) >> modifiedUser
            'Inc' == fetchedUser.lastName
    }

    def 'Test delete method of UserService' () {
        when:
            userService.delete(user.id)
        then:
            1 * userRepository.delete(_)
    }
}

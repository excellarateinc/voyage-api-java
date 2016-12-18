package launchpad.user

import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class UserServiceSpec extends Specification {
    User user
    User modifiedUser
    UserRepository userRepository = Mock()
    UserService userService = new UserService(userRepository)

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
        modifiedUser = new User(id: 1, firstName: 'LSS', lastName: 'Inc')
    }

    def 'Test the list method of UserService' () {
        when:
            Iterable<User> userList = userService.listAll()
        then:
            1 * userRepository.findAll() >> [user]
            userList.size() == 1
    }

    def 'Test save method of UserService' () {
        when:
            User savedUser = userService.save(user)
        then:
            1 * userRepository.save({User user -> user.firstName == 'LSS'}) >> modifiedUser
            savedUser.lastName == 'Inc'
    }

    def 'Test update method of UserService' () {
        when:
            User modifiedUser = userService.update(user)
        then:
            1 * userRepository.save({User user -> user.lastName == 'India'}) >> modifiedUser
            modifiedUser.lastName == 'Inc'
    }

    def 'Test find method of UserService' () {
        when:
            User fetchedUser = userService.get(1)
        then:
            1 * userRepository.findOne(_) >> modifiedUser
            fetchedUser.lastName == 'Inc'
    }

    def 'Test delete method of UserService' () {
        when:
            userService.delete(1)
        then:
            1 * userRepository.delete(_)
    }
}

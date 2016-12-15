package launchpad.user

import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by dhanumandla on 15/12/16.
 */
@Stepwise
class UserServiceSpec extends Specification {
    private User user
    private User modifiedUser
    private UserService classUnderTest = new UserService()
    private UserRepository userRepository = Mock()

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
        modifiedUser = new User(id: 1, firstName: 'LSS', lastName: 'Inc')

        classUnderTest.userRepository = userRepository
    }

    def 'Test the list method of UserService' () {
        when:
            List<User> userList = classUnderTest.listAll()
        then:
            1 * userRepository.findAll() >> [user]
            userList.size() == 1
    }

    def 'Test save method of UserService' () {
        when:
            User savedUser = classUnderTest.save(user)
        then:
            1 * userRepository.save({User user -> user.firstName == 'LSS'}) >> modifiedUser
            savedUser.lastName == 'Inc'
    }

    def 'Test update method of UserService' () {
        when:
            User modifiedUser = classUnderTest.update(user)
        then:
            1 * userRepository.save({User user -> user.lastName == 'India'}) >> modifiedUser
            modifiedUser.lastName == 'Inc'
    }

    def 'Test find method of UserService' () {
        when:
            User fetchedUser = classUnderTest.get(1)
        then:
            1 * userRepository.findOne(_) >> modifiedUser
            fetchedUser.lastName == 'Inc'
    }

    def 'Test delete method of UserService' () {
        when:
            classUnderTest.delete(1)
        then:
            1 * userRepository.delete(_)
    }
}

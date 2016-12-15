package launchpad.user

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

/**
 * Unit tests to test the UserController by Mocking the 'UserService' external dependency.
 */
class UserControllerSpec extends Specification {
    private User user, modifiedUser
    def userService = Mock(UserService)
    def classUnderTest = new UserController(userService)

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
        modifiedUser = new User(id: 1, firstName: 'LSS', lastName: 'Inc')
    }

    def 'Test to validate LIST method is fetching data from UserService' () {
        when:
            ResponseEntity<User> users = classUnderTest.list()
        then:
            1 * userService.listAll() >> [user]
            users != null
            users.statusCode == HttpStatus.OK

        when:
            classUnderTest.list()
        then:
            1 * userService.listAll() >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate FIND method is fetching data from UserService' () {
        when:
            ResponseEntity<User> user = classUnderTest.get(1)
        then:
            1 * userService.get(1) >> user
            user != null
            user.statusCode == HttpStatus.OK

        when:
            classUnderTest.get(1)
        then:
            1 * userService.get(1) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate CREATE method is fetching data from UserService' () {
        when:
            ResponseEntity<User> newUser = classUnderTest.save(user)
        then:
            1 * userService.save(user) >> modifiedUser
            newUser != null
            newUser.statusCode == HttpStatus.OK

        when:
            classUnderTest.save(user)
        then:
            1 * userService.save(user) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate UPDATE method is fetching data from UserService' () {
        when:
            ResponseEntity<User> updatedUser = classUnderTest.update(modifiedUser)
        then:
            1 * userService.update(modifiedUser) >> modifiedUser
            updatedUser != null
            updatedUser.statusCode == HttpStatus.OK

        when:
            classUnderTest.update(modifiedUser)
        then:
            1 * userService.update(modifiedUser) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate DELETE method is fetching data from UserService' () {
        when:
            ResponseEntity response = classUnderTest.delete(1)
        then:
            1 * userService.delete(1)
            response.statusCode == HttpStatus.OK

        when:
            classUnderTest.delete(1)
        then:
            1 * userService.delete(1) >> {throw new Exception()}
            thrown(Exception)
    }

}

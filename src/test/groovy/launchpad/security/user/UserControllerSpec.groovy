package launchpad.security.user

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

// TODO Needs to be rewritten to focus on inputs and outputs. Right now these tests are not validating the entire JSON response or JSON request
// TODO Remove the existing Exception tests because they do NOTHING! What's the point of these at all?
// TODO Add NEW exception tests for exceptions that are actually thrown by the Service classes (ImmutableRecordException,
//      UnknownIdentifierException, ValidationException...)
class UserControllerSpec extends Specification {
    User user
    User modifiedUser
    UserService userService = Mock(UserService)
    UserController userController = new UserController(userService)

    def setup() {
        user = new User(id:1, firstName:'LSS', lastName:'India')
        modifiedUser = new User(id:1, firstName:'LSS', lastName:'Inc')
    }

    def 'Test to validate LIST method is fetching data from UserService'() {
        when:
            ResponseEntity<User> users = userController.list()
        then:
            1 * userService.listAll() >> [user]
            users != null
            HttpStatus.OK == users.statusCode

        when:
            userController.list()
        then:
            1 * userService.listAll() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'get - fetch data from UserService'() {
        when:
            ResponseEntity<User> user = userController.get(1)
        then:
            1 * userService.get(1) >> user
            user != null
            HttpStatus.OK == user.statusCode
            'LSS' == user.body.firstName
            'India' == user.body.lastName

        when:
            userController.get(1)
        then:
            1 * userService.get(1) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'save - calling UserService to save object'() {
        when:
            ResponseEntity<User> response = userController.save(user)
        then:
            1 * userService.saveDetached(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            '/api/v1/users/1' == response.headers.location[0]
            'LSS' == response.body.firstName
            'Inc' == response.body.lastName

        when:
            userController.save(user)
        then:
            1 * userService.saveDetached(user) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'update - calling UserService to save incoming object'() {
        when:
            ResponseEntity<User> updatedUser = userController.update(modifiedUser)
        then:
            1 * userService.saveDetached(modifiedUser) >> modifiedUser
            updatedUser != null
            HttpStatus.OK == updatedUser.statusCode

        when:
            userController.update(modifiedUser)
        then:
            1 * userService.saveDetached(modifiedUser) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'delete - calling UserService with the user id'() {
        when:
            ResponseEntity response = userController.delete(1)
        then:
            1 * userService.delete(1)
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            userController.delete(1)
        then:
            1 * userService.delete(1) >> { throw new Exception() }
            thrown(Exception)
    }

}

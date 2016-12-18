package launchpad.user

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class UserControllerSpec extends Specification {
    User user
    User modifiedUser
    UserService userService = Mock(UserService)
    UserController userController = new UserController(userService)

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
        modifiedUser = new User(id: 1, firstName: 'LSS', lastName: 'Inc')
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
            1 * userService.listAll() >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate FIND method is fetching data from UserService'() {
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
            1 * userService.get(1) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate CREATE method is fetching data from UserService'() {
        when:
            ResponseEntity<User> response = userController.save(user)
        then:
            1 * userService.save(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            "/v1/users/1" == response.headers.getLocation().toString()
            'LSS' == response.body.firstName
            'Inc' == response.body.lastName

        when:
            userController.save(user)
        then:
            1 * userService.save(user) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate UPDATE method is fetching data from UserService'() {
        when:
            ResponseEntity<User> updatedUser = userController.update(modifiedUser)
        then:
            1 * userService.update(modifiedUser) >> modifiedUser
            updatedUser != null
            HttpStatus.OK == updatedUser.statusCode

        when:
            userController.update(modifiedUser)
        then:
            1 * userService.update(modifiedUser) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate DELETE method is fetching data from UserService'() {
        when:
            ResponseEntity response = userController.delete(1)
        then:
            1 * userService.delete(1)
            HttpStatus.OK == response.statusCode

        when:
            userController.delete(1)
        then:
            1 * userService.delete(1) >> {throw new Exception()}
            thrown(Exception)
    }

}

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
            users.statusCode == HttpStatus.OK

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
            user.statusCode == HttpStatus.OK
            user.body.firstName == 'LSS'
            user.body.lastName == 'India'

        when:
            userController.get(1)
        then:
            1 * userService.get(1) >> {throw new Exception()}
            thrown(Exception)
    }

    def 'Test to validate CREATE method is fetching data from UserService'() {
        when:
            ResponseEntity<User> newUser = userController.save(user)
        then:
            1 * userService.save(user) >> modifiedUser
            newUser != null
            newUser.statusCode == HttpStatus.OK
            newUser.body.firstName == 'LSS'
            newUser.body.lastName == 'Inc'

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
            updatedUser.statusCode == HttpStatus.OK

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
            response.statusCode == HttpStatus.OK

        when:
            userController.delete(1)
        then:
            1 * userService.delete(1) >> {throw new Exception()}
            thrown(Exception)
    }

}

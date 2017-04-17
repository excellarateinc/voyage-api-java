package voyage.profile

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.user.User
import voyage.security.verify.VerifyService

class ProfileControllerSpec extends Specification {
    User user
    User modifiedUser
    ProfileService profileService = Mock(ProfileService)
    VerifyService userVerifyService = Mock(VerifyService)
    ProfileController profileController = new ProfileController(profileService, userVerifyService)

    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
    }

    def 'Test to validate create method'() {
        when:
            ResponseEntity<User> response = profileController.create(user)
        then:
            1 * profileService.create(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/profile' == response.headers.location[0]

        when:
            profileController.create(user)
        then:
            1 * profileService.create(user) >> { throw new Exception() }
            thrown(Exception)
    }
}

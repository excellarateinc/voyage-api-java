package voyage.profile

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.crypto.CryptoService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService
import voyage.test.AbstractIntegrationTest

import javax.mail.internet.MimeMessage

class ProfileControllerIntegrationSpec extends AbstractIntegrationTest {
    private GreenMail greenMailSMTP

    @Autowired
    private UserService userService

    @Autowired
    private CryptoService cryptoService

    def setup() {
        ServerSetup setup = new ServerSetup(3025, 'localhost', ServerSetup.PROTOCOL_SMTP)
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    def '/api/v1/profile POST - Profile create '() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'Test&1234')
            user.phones = [new UserPhone(phoneNumber:'+16124590457', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity responseEntity = POST('/api/v1/profile', httpEntity, String, standardClient)
            User savedUser = userService.findByUsername(user.username)

        then:
            responseEntity.statusCode.value() == 201
            savedUser.firstName == 'Test1'
            savedUser.lastName == 'User'
            savedUser.username == 'username'
            savedUser.email == 'test@test.com'
            cryptoService.hashMatches('Test&1234', savedUser.password)
            savedUser.phones.size() == 1
            savedUser.isVerifyRequired
            savedUser.isEnabled
            savedUser.phones[0].phoneType == PhoneType.MOBILE
            savedUser.phones[0].phoneNumber == '+16124590457'

            MimeMessage[] emails = greenMailSMTP.receivedMessages
            emails.size() == 1
            emails[0].allRecipients.size() == 1
    }

    def '/api/v1/profile POST - Profile create fails with error due to username already in use'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
            user.phones = [new UserPhone(phoneNumber:'+1-111-111-1111', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profile', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_username_already_in_use'
            responseEntity.body[0].errorDescription == 'Username already in use by another user. Please choose a different username.'
    }

    def '/api/v1/profile POST - Profile create fails with error due to missing required values'() {
        given:
            User user = new User()
            user.phones = [new UserPhone(phoneNumber:'+1-800-888-8888', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profile', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 4
            responseEntity.body.find { it.error == 'password.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'firstname.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'username.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'lastname.may_not_be_empty' && it.errorDescription == 'may not be empty' }
    }

    def '/api/v1/profile POST - Profile create fails with error due to email format invalid'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username4', email:'test@', password:'Test&1234')
            user.phones = [new UserPhone(phoneNumber:'+1-800-888-8888', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profile', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == 'email.not_a_well-formed_email_address'
            responseEntity.body[0].errorDescription == 'not a well-formed email address'
    }

    def '/api/v1/profile POST - Profile create fails with error due to missing mobile phone'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username2', email:'test@test.com', password:'Test&1234')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profile', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_mobile_phone_required'
            responseEntity.body[0].errorDescription == 'At least one mobile phone is required for a new profile'
    }

    def '/api/v1/profile POST - Profile create fails with error due to > 5 phones'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username2', email:'test@test.com', password:'Test&1234')
            user.phones = [
                new UserPhone(phoneNumber:'+1205-111-1111', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1222-222-2222', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1333-333-3333', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1444-444-4444', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1555-555-5555', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1666-666-6666', phoneType:PhoneType.MOBILE),
            ]

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profile', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_too_many_phones'
            responseEntity.body[0].errorDescription == 'Too many phones have been added to the profile. Maximum of 5.'
    }
}

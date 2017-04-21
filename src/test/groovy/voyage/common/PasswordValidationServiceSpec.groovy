package voyage.common

import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * Created by user on 4/19/2017.
 */
class PasswordValidationServiceSpec extends Specification {
    private final PasswordValidationService passwordValidationService = new PasswordValidationService()
    def 'basic string input for password'() {
        when:
        boolean result = passwordValidationService.validate('password')
        then:
        InvalidPasswordException e = thrown()
        e.message == 'The password did not met the requirements'
        e.errorCode == '400_password_invalid'
    }
    def 'basic number input for password'() {
        when:
        boolean result = passwordValidationService.validate('12345678')
        then:
        InvalidPasswordException e = thrown()
        e.message == 'The password did not met the requirements'
        e.errorCode == '400_password_invalid'
    }
    def 'string and number input for password'() {
        when:
        boolean result = passwordValidationService.validate('abcd1234')
        then:
        InvalidPasswordException e = thrown()
        e.message == 'The password did not met the requirements'
        e.errorCode == '400_password_invalid'
    }
    def 'string  number and special char input for password without uppercase'() {
        when:
        boolean result = passwordValidationService.validate('abcd&1234')
        then:
        InvalidPasswordException e = thrown()
        e.message == 'The password did not met the requirements'
        e.errorCode == '400_password_invalid'
    }
    def 'string  number and special char input for password with uppercase'() {
        when:
        boolean result = passwordValidationService.validate('Abcd&1234')
        then:
        result == true
    }
    def 'string  number and special char input for password with uppercase and whitespace'() {
        when:
        boolean result = passwordValidationService.validate('Abcd&1234 ')
        then:
        InvalidPasswordException e = thrown()
        e.message == 'The password did not met the requirements'
        e.errorCode == '400_password_invalid'
    }
}

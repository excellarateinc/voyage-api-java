package voyage.security.user

import spock.lang.Specification

class PasswordValidationServiceSpec extends Specification {
    private final PasswordValidationService passwordValidationService = new PasswordValidationService()
    def 'basic string input for password'() {
        when:
        passwordValidationService.validate('password')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
    def 'basic number input for password'() {
        when:
        passwordValidationService.validate('12345678')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
    def 'string and number input for password'() {
        when:
        passwordValidationService.validate('Test1234')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
    def 'string  number and special char input for password without uppercase'() {
        when:
        passwordValidationService.validate('test@1234')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
    def 'string  number and special char input for password without digits'() {
        when:
        passwordValidationService.validate('Test@test')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
    def 'string  number and special char input for password with uppercase'() {
        when:
        boolean result = passwordValidationService.validate('Test&1234')
        then:
        result == true
    }
    def 'string  number and special char input for password with uppercase and whitespace'() {
        when:
        passwordValidationService.validate('Test&1234 ')
        then:
        InvalidPasswordException e = thrown()
        e.errorCode == '400_password_invalid_the password did not meet the requirements'
    }
}

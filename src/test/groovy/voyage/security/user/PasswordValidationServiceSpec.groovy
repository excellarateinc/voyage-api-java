package voyage.security.user

import spock.lang.Specification

class PasswordValidationServiceSpec extends Specification {
    private final PasswordValidationService passwordValidationService = new PasswordValidationService()

    def 'basic string input for password'() {
        when:
            passwordValidationService.validate('password')

        then:
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
    }

    def 'basic number input for password'() {
        when:
            passwordValidationService.validate('12345678')

        then:
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
    }

    def 'string and number input for password'() {
        when:
            passwordValidationService.validate('Test1234')

        then:
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
    }

    def 'string  number and special char input for password without uppercase'() {
        when:
            passwordValidationService.validate('test@1234')

        then:
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
    }

    def 'string  number and special char input for password without digits'() {
        when:
            passwordValidationService.validate('Test@test')

        then:
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
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
            WeakPasswordException e = thrown()
            e.errorCode == '400_weak_password'
            e.message == 'Password should contain 1 uppercase, ' +
                    '1 lowercase, 1 special character and should not contain any whitespace.'
    }
}

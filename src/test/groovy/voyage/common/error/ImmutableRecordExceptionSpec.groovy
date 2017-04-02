package voyage.common.error

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.security.user.UsernameAlreadyInUseException

class ImmutableRecordExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new UsernameAlreadyInUseException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_username_already_in_use'
            ex.message == 'Username already in use by another user. Please choose a different username.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new UsernameAlreadyInUseException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_username_already_in_use'
            ex.message == 'TEST MESSAGE'
    }
}

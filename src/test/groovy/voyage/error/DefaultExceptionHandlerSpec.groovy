package voyage.error

import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.RequestAttributes
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Path

class DefaultExceptionHandlerSpec extends Specification {

    def 'handle() AccessDeniedException returns an Unauthorized error'() {
        when:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(new AccessDeniedException('test'))

        then:
            responseEntity.statusCodeValue == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def 'handle() ConstraintViolationException returns an Bad Request error'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)
            def emailConstraint = Mock(ConstraintViolation)
            def emailConstraintPath = Mock(Path)

            def notNullConstraint = Mock(ConstraintViolation)
            def notNullConstraintPath = Mock(Path)

        when:
            Set violations = new LinkedHashSet<? extends ConstraintViolation<?>>()
            violations.add(emailConstraint)
            violations.add(notNullConstraint)
            ConstraintViolationException exception = new ConstraintViolationException(violations)
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            emailConstraint.message >> 'Invalid email address'
            emailConstraint.propertyPath >> emailConstraintPath
            emailConstraintPath.toString() >> 'field.name'

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 2
            responseEntity.body[0].error == 'field.name.invalid_email_address'
            responseEntity.body[0].errorDescription == 'Invalid email address'

            notNullConstraint.message >> 'Phone number required'
            notNullConstraint.propertyPath >> notNullConstraintPath
            notNullConstraintPath.toString() >> 'field.phone'

            responseEntity.body[1].error == 'field.phone.phone_number_required'
            responseEntity.body[1].errorDescription == 'Phone number required'
    }

    def 'handle() MethodArgumentNotValidException returns an Bad Request error'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)
            def exception = Mock(MethodArgumentNotValidException)

            BindingResult bindingResult = Mock(BindingResult)
            FieldError usernameRequiredError = Mock(FieldError)
            FieldError firstNameRequiredError = Mock(FieldError)

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            exception.bindingResult >> bindingResult
            bindingResult.fieldErrors >> [usernameRequiredError, firstNameRequiredError]
            usernameRequiredError.code >> 'field.username'
            usernameRequiredError.defaultMessage >> 'Username is required'
            firstNameRequiredError.code >> 'field.firstName'
            firstNameRequiredError.defaultMessage >> 'First Name is required'


        responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 2
            responseEntity.body[0].error == 'field.username'
            responseEntity.body[0].errorDescription == 'Username is required'
            responseEntity.body[1].error == 'field.firstName'
            responseEntity.body[1].errorDescription == 'First Name is required'
    }

    def 'handle() AppException returns an Bad Request error'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)
            AppException exception = new AppException(HttpStatus.BAD_REQUEST, 'Default message')

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Default message'
    }

    def 'handle() Exception returns a 500 Internal Server error'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)
            Exception exception = new Exception('Default message')

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            responseEntity.statusCodeValue == 500
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '500_internal_server_error'
            responseEntity.body[0].errorDescription == 'java.lang.Exception: Default message'
    }

    def 'handleError() processes a HttpServlet exception'() {
        given:
            ErrorAttributes errorAttributes = Mock(ErrorAttributes)
            Map errorMap = [status: 400, error: 'test error', message: 'test message']
            DefaultExceptionHandler handler = new DefaultExceptionHandler(errorAttributes)
            
            def httpServletRequest = Mock(HttpServletRequest)
            def httpServletResponse = Mock(HttpServletResponse)

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handleError(httpServletRequest, httpServletResponse)

        then:
            errorAttributes.getErrorAttributes(_ as RequestAttributes, false) >> errorMap
            httpServletResponse.status >> HttpStatus.BAD_REQUEST.value()

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == '400 test error. test message'
    }

    def 'handleError() routes to handle(AppException) if an AppException is found in the request attributes'() {
        given:
            DefaultExceptionHandler handler = new DefaultExceptionHandler(null)

            def httpServletRequest = Mock(HttpServletRequest)
            def httpServletResponse = Mock(HttpServletResponse)

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handleError(httpServletRequest, httpServletResponse)

        then:
            httpServletRequest.getAttribute('javax.servlet.error.exception') >> new AppException(HttpStatus.BAD_REQUEST, 'Test message')

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Test message'
    }
}

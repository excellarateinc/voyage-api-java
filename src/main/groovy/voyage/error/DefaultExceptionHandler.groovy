package voyage.error

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolationException

@ControllerAdvice
@RestController
class DefaultExceptionHandler implements ErrorController {
    private static final Logger LOG = LoggerFactory.getLogger(this.getClass())
    private final ErrorAttributes errorAttributes

    String errorPath = '/error' // Overrides ErrorController.getErrorPath()

    @Autowired
    DefaultExceptionHandler(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(AccessDeniedException ignore) {
        ErrorResponse errorResponse = new ErrorResponse(
            error:ErrorUtils.getErrorCode(HttpStatus.UNAUTHORIZED.value()),
            errorDescription:'401 Unauthorized. Access Denied',
        )
        return new ResponseEntity([errorResponse], HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(ConstraintViolationException e) {
        List errorResponses = []
        e.constraintViolations.each { violation ->
            ErrorResponse errorResponse = new ErrorResponse(
                error:ErrorUtils.formatErrorCode(violation.propertyPath.toString() + '.' + violation.message),
                errorDescription:violation.message,
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(MethodArgumentNotValidException e) {
        List errorResponses = []
        e.bindingResult.fieldErrors.each { fieldError ->
            ErrorResponse errorResponse = new ErrorResponse(
                error:fieldError.code,
                errorDescription:fieldError.defaultMessage,
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(AppException e) {
        ErrorResponse errorResponse = new ErrorResponse(
            error:e.errorCode,
            errorDescription:e.message,
        )
        return new ResponseEntity([errorResponse], e.httpStatus)
    }

    @ExceptionHandler(value = Exception)
    ResponseEntity<Iterable<ErrorResponse>> handle(Exception e) {
        LOG.error('Unexpected error occurred', e)
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        ErrorResponse errorResponse = new ErrorResponse(
            error:ErrorUtils.getErrorCode(httpStatus.value()),
            errorDescription:e.toString(),
        )
        return new ResponseEntity([errorResponse], httpStatus)
    }

    @RequestMapping(value = '/error')
    ResponseEntity<Iterable<ErrorResponse>> handleError(HttpServletRequest request, HttpServletResponse response) {
        Map errorMap = getErrorAttributes(request, false)

        // Handle AppExceptions by the definition embedded in the exception
        Exception exception = (Exception)request.getAttribute('javax.servlet.error.exception')
        if (exception instanceof AppException) {
            return handle((AppException)exception)
        }

        // Handle unknown exceptions based on the error details given
        String errorCode = ErrorUtils.getErrorCode((int)errorMap.status)
        String errorMessage = "${errorMap.status} ${errorMap.error}. ${errorMap.message}"
        ErrorResponse errorResponse = new ErrorResponse(
            error:errorCode,
            errorDescription:errorMessage,
        )
        return new ResponseEntity([errorResponse], HttpStatus.valueOf(response.status))
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace = false) {
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request)
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace)
    }
}

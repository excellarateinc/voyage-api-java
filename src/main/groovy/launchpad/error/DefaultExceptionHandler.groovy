package launchpad.error

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private static final String UNDER_SCORE = '_'
    private final ErrorAttributes errorAttributes

    String errorPath = '/error' // Overrides ErrorController.getErrorPath()

    @Autowired
    DefaultExceptionHandler(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(ConstraintViolationException e) {
        List errorResponses = []
        e.constraintViolations.each { violation ->
            ErrorResponse errorResponse = new ErrorResponse(
                code:formatToCode(violation.propertyPath.toString() + '.' + violation.message),
                description:violation.message,
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
                code:fieldError.code,
                description:fieldError.defaultMessage,
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(UnknownIdentifierException e) {
        ErrorResponse errorResponse = new ErrorResponse(
            code:getErrorCode(HttpStatus.NOT_FOUND),
            description:e.message,
        )
        return new ResponseEntity([errorResponse], HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = Exception)
    ResponseEntity<Iterable<ErrorResponse>> handle(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                code:'error.500_internal_server_error',
                description:e.toString(),
        )
        return new ResponseEntity([errorResponse], HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @RequestMapping(value = '/error')
    ResponseEntity<Iterable<ErrorResponse>> handleError(HttpServletRequest request, HttpServletResponse response) {
        Map errorMap = getErrorAttributes(request, false)
        String errorCode = getErrorCode((int)errorMap.status)
        String errorMessage = "${errorMap.status} ${errorMap.error}. ${errorMap.message}"
        ErrorResponse errorResponse = new ErrorResponse(
                code:errorCode,
                description:errorMessage,
        )
        return new ResponseEntity([errorResponse], HttpStatus.valueOf(response.status))
    }

    private static String getErrorCode(int httpStatusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        String errorCode = httpStatus.value() + UNDER_SCORE + httpStatus.name()
        return formatToCode(errorCode)
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace = false) {
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request)
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace)
    }

    private static String formatToCode(String description) {
        return description.toLowerCase().replace(' ', UNDER_SCORE)
    }
}

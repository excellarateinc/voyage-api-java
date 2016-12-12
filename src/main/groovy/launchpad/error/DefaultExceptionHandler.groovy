package launchpad.error

import org.apache.commons.lang3.StringUtils
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
    private ErrorAttributes errorAttributes

    @Autowired
    DefaultExceptionHandler(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(ConstraintViolationException e) {
        def errorResponses = []
        e.constraintViolations.each { violation ->
            def errorResponse = new ErrorResponse(
                code: formatToCode(violation.getPropertyPath().toString() + "." + violation.message),
                description: violation.message
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(MethodArgumentNotValidException e) {
        def errorResponses = []
        e.bindingResult.fieldErrors.each { fieldError ->
            def errorResponse = new ErrorResponse(
                code: fieldError.code,
                description: fieldError.defaultMessage
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<Iterable<ErrorResponse>> handle(Exception e) {
        def errorResponse = new ErrorResponse(
                code: "error.500_internal_server_error",
                description: e.toString()
        )
        return new ResponseEntity([errorResponse], HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @RequestMapping(value = "/error")
    ResponseEntity<Iterable<ErrorResponse>> handleError(HttpServletRequest request, HttpServletResponse response) {
        def errorMap = getErrorAttributes(request, false)
        def errorCode = getErrorCode((int)errorMap.status)
        def errorMessage = "${errorMap.status} ${errorMap.error}. ${errorMap.message}"
        def errorResponse = new ErrorResponse(
                code: errorCode,
                description: errorMessage
        )
        return new ResponseEntity([errorResponse], HttpStatus.valueOf(response.getStatus()))
    }

    @Override
    String getErrorPath() {
        return "/error"
    }

    private static String getErrorCode(int httpStatusCode) {
        def httpStatus = HttpStatus.valueOf(httpStatusCode)
        def errorCode = httpStatus.value() + "_" + httpStatus.name()
        return formatToCode(errorCode)
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace = false) {
        def requestAttributes = new ServletRequestAttributes(request)
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace)
    }

    private static String formatToCode(String description) {
        return description.toLowerCase().replace(" ", "_")
    }
}

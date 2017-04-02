package voyage.common.error

import spock.lang.Specification

class ErrorUtilsSpec extends Specification {

    def 'getErrorCode returns a properly formatted error code for a 404 status code'() {
        when:
            String errorCode = ErrorUtils.getErrorCode(404)
        then:
            errorCode == '404_not_found'
    }

    def 'getErrorCode returns a properly formatted error code for a 404 status code and custom description'() {
        when:
            String errorCode = ErrorUtils.getErrorCode(404, 'test description')
        then:
            errorCode == '404_test_description'
    }

    def 'formatErrorCode properly handles spaces'() {
        when:
            String errorCode = ErrorUtils.formatErrorCode('test description here')
        then:
            errorCode == 'test_description_here'
    }
}

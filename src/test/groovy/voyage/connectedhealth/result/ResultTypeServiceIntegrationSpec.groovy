package voyage.connectedhealth.result

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class ResultTypeServiceIntegrationSpec extends Specification {
    @Autowired
    ResultTypeService resultTypeService

    def 'findAll - find all result types'() {
        when:
        List<ResultType> resultType = resultTypeService.findAll()

        then:
        resultType.size() == 3
    }

    def 'get - get a result by ID'() {
        when:
        ResultType resultType = resultTypeService.get(1L)

        then:
        resultType.id == 1L
    }
}

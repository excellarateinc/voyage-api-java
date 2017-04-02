package voyage.security.audit

import spock.lang.Specification
import voyage.common.error.UnknownIdentifierException

class ActionLogServiceSpec extends Specification {
    ActionLog actionLog
    ActionLog modifiedActionLog
    ActionLogRepository actionLogRepository = Mock()
    ActionLogService actionLogService = new ActionLogService(actionLogRepository)

    def setup() {
        actionLog = new ActionLog(clientProtocol:'http', clientIpAddress:'127.0.0.1', httpMethod:'GET', url:'http://test.request.url')
        modifiedActionLog = new ActionLog(clientProtocol:'http', clientIpAddress:'127.0.0.1', httpMethod:'GET', url:'http://test.request.url')
    }

    def 'get - calls the actionLogRepository.findOne' () {
        setup:
            actionLogRepository.findOne(_) >> actionLog
        when:
            ActionLog fetchedActionLog = actionLogService.get(1)
        then:
            'http' == fetchedActionLog.clientProtocol
            '127.0.0.1' == fetchedActionLog.clientIpAddress
    }

    def 'get - UnknownIdentifierException' () {
        setup:
            actionLogRepository.findOne(_) >> null
        when:
            actionLogService.get(1)
        then:
            thrown(UnknownIdentifierException)
    }
}

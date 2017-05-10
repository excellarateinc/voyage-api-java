package voyage.security.audit

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.common.error.UnknownIdentifierException

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class ActionLogService {
    private final ActionLogRepository actionLogRepository

    ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository
    }

    ActionLog get(@NotNull Long id) {
        ActionLog actionLog = actionLogRepository.findOne(id)
        if (!actionLog) {
            throw new UnknownIdentifierException()
        }
        return actionLog
    }

    ActionLog saveDetached(@Valid ActionLog actionLog) {
        if (actionLog.id) {
            ActionLog existingActionLog = get(actionLog.id)
            existingActionLog.with {
                clientIpAddress = actionLog.clientIpAddress
                clientProtocol = actionLog.clientProtocol
                durationMs = actionLog.durationMs
                httpMethod = actionLog.httpMethod
                httpStatus = actionLog.httpStatus
                username = actionLog.username
                client = actionLog.client
                user = actionLog.user
                requestHeaders = actionLog.requestHeaders
                requestBody = actionLog.requestBody
                responseHeaders = actionLog.responseHeaders
                responseBody = actionLog.responseBody
                url = actionLog.url
                lastModifiedDate = new Date()
            }
            return actionLogRepository.save(existingActionLog)
        }
        actionLog.createdDate = new Date()
        actionLog.lastModifiedDate = new Date()
        return actionLogRepository.save(actionLog)
    }
}

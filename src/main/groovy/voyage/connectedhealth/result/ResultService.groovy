package voyage.connectedhealth.result

import groovy.time.TimeCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import voyage.connectedhealth.healthorg.HealthOrganization
import voyage.connectedhealth.healthorg.HealthOrganizationService
import voyage.connectedhealth.user.UserDeviceService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.transaction.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Transactional
@Service
class ResultService {
    private ResultRepository resultRepository
    private HealthOrganizationService healthOrganizationService
    private UserService userService
    private ResultTypeService resultTypeService
    private UserDeviceService userDeviceService
    private static final int RECENT_RESULT_OFFSET_DAYS = 7

    @Autowired
    ResultService(ResultRepository resultRepository,
                  HealthOrganizationService healthOrganizationService,
                  UserService userService,
                  ResultTypeService resultTypeService,
                  UserDeviceService userDeviceService) {
        this.resultRepository = resultRepository
        this.healthOrganizationService = healthOrganizationService
        this.userService = userService
        this.resultTypeService = resultTypeService
        this.userDeviceService = userDeviceService
    }

    Result get(long id) {
        return resultRepository.findByIdAndIsDeletedFalse(id)
    }

    Page<Result> findRecent(User user, Pageable pageable) {
        Date asOfDate = null
        use(TimeCategory) {
            asOfDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()) - RECENT_RESULT_OFFSET_DAYS.days
        }
        return resultRepository.findByUserAndCreatedDateGreaterThanAndIsDeletedFalse(user, asOfDate, pageable)
    }

    Page<Result> findRecent(User user, HealthOrganization healthOrganization, Pageable pageable) {
        Date asOfDate = null
        use(TimeCategory) {
            asOfDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()) - RECENT_RESULT_OFFSET_DAYS.days
        }
        return resultRepository.findByUserAndHealthOrganizationAndCreatedDateGreaterThanAndIsDeletedFalse(user, healthOrganization, asOfDate, pageable)
    }

    Page<Result> findAll(User user, Pageable pageable) {
        return resultRepository.findByUserAndIsDeletedFalse(user, pageable)
    }

    Page<Result> findAll(User user, HealthOrganization healthOrganization, Pageable pageable) {
        return resultRepository.findByUserAndHealthOrganizationAndIsDeletedFalse(user, healthOrganization, pageable)
    }

    Page<Result> findAll(User user, HealthOrganization healthOrganization, ResultType resultType, Pageable pageable) {
        return resultRepository.findByUserAndHealthOrganizationAndResultTypeAndIsDeletedFalse(user, healthOrganization, resultType, pageable)
    }

    Page<Result> findAll(User user, ResultType resultType, Pageable pageable) {
        return resultRepository.findByUserAndResultTypeAndIsDeletedFalse(user, resultType, pageable)
    }

    Result save(Result resultIn) {
        Result result
        if (resultIn.id) {
            result = get(resultIn.id)
        } else {
            result = new Result()
            result.user = userService.currentUser
            result.healthOrganization = healthOrganizationService.get(resultIn.healthOrganization.id)
            result.resultType = resultTypeService.get(resultIn.resultType.id)
            result.userDevice = userDeviceService.get(resultIn.userDevice.id)
            result.resultMethod = resultIn.resultMethod
            result.value = resultIn.value
            result.entryDate = new Date()
        }
        result.with {
            result.comment = resultIn.comment
            result.isValid = resultIn.isValid
        }
        return resultRepository.save(result)
    }
}

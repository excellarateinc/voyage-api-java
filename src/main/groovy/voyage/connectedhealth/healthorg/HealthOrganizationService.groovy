package voyage.connectedhealth.healthorg

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import voyage.core.error.AppException
import voyage.security.user.User

import javax.transaction.Transactional

@Transactional
@Service
class HealthOrganizationService {

    private HealthOrganizationRepository healthOrganizationRepository
    private HealthOrganizationUserRepository healthOrganizationUserRepository

    @Autowired
    HealthOrganizationService(HealthOrganizationRepository healthOrganizationRepository,
                              HealthOrganizationUserRepository healthOrganizationUserRepository) {
        this.healthOrganizationRepository = healthOrganizationRepository
        this.healthOrganizationUserRepository = healthOrganizationUserRepository
    }

    HealthOrganization get(long id) {
        return healthOrganizationRepository.findByIdAndIsDeletedFalse(id)
    }

    List<HealthOrganization> findByUser(User user) {
        List<HealthOrganizationUser> healthOrgUsers =
                healthOrganizationUserRepository.findByUserAndUserIsDeletedFalseAndHealthOrganizationIsDeletedFalse(user)
        return healthOrgUsers.collect{it.healthOrganization}
    }

    Page<HealthOrganization> findAll(Pageable pageable) {
        return healthOrganizationRepository.findByIsDeletedFalse(pageable)
    }

    HealthOrganization save(HealthOrganization healthOrganizationIn) {
        HealthOrganization healthOrganization
        if (healthOrganizationIn.id) {
            healthOrganization = get(healthOrganizationIn.id)
        } else {
            healthOrganization = new HealthOrganization()
        }
        healthOrganization.with {
            name = healthOrganizationIn.name
            mainPhoneNumber = healthOrganizationIn.mainPhoneNumber
            city = healthOrganizationIn.city
            state = healthOrganizationIn.state
            logoUrl = healthOrganizationIn.logoUrl
        }
        healthOrganizationRepository.save(healthOrganization)
    }

    HealthOrganizationUser assign(HealthOrganization healthOrganizationIn, User userIn) {
        HealthOrganization healthOrganization = healthOrganizationRepository.findByIdAndIsDeletedFalse(healthOrganizationIn.id)
        if (!healthOrganization) {
            throw new AppException('Health Organization not found')
        }

        HealthOrganizationUser healthOrganizationUser =
                healthOrganizationUserRepository.findByUserAndUserIsDeletedFalseAndHealthOrganizationAndHealthOrganizationIsDeletedFalse(userIn, healthOrganization)
        if (!healthOrganizationUser) {
            healthOrganizationUser = new HealthOrganizationUser()
            HealthOrganizationUserKey id = new HealthOrganizationUserKey()
            id.healthOrganizationId = healthOrganization.id
            id.userId = userIn.id
            healthOrganizationUser.id = id
            healthOrganizationUser.user = userIn
            healthOrganizationUser.healthOrganization = healthOrganization
            healthOrganizationUser.isShareData = false
            healthOrganizationUser.isPrimary = false
            healthOrganizationUser.asOfDate = new Date()
            healthOrganizationUserRepository.save(healthOrganizationUser)
            return healthOrganizationUser
        } else {
            throw new AppException('User already assigned to Health Organization')
        }
    }

    List<HealthOrganizationUserSettings> findAllUserSettings(User user) {
        List<HealthOrganizationUser> healthOrgUsers =
                healthOrganizationUserRepository.findByUserAndUserIsDeletedFalseAndHealthOrganizationIsDeletedFalse(user)
        return healthOrgUsers.collect{it ->
            new HealthOrganizationUserSettings(healthOrganization: it.healthOrganization, isPrimary: it.isPrimary, isShareData: it.isShareData)}
    }

    void updateUserSettings(HealthOrganizationUserSettings healthOrganizationUserSettings, User user) {
        List<HealthOrganizationUser> userHealthOrgs =
                healthOrganizationUserRepository.findByUserAndUserIsDeletedFalseAndHealthOrganizationIsDeletedFalse(user)
        userHealthOrgs.each {it ->
            if (it.healthOrganization.id == healthOrganizationUserSettings.healthOrganization.id) {
                it.isShareData = healthOrganizationUserSettings.isShareData
                it.isPrimary = healthOrganizationUserSettings.isPrimary
            } else {
                it.isPrimary = false
            }
        }
        healthOrganizationUserRepository.save(userHealthOrgs)
    }
}

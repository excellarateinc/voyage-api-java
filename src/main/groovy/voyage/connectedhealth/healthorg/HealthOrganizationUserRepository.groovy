package voyage.connectedhealth.healthorg

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import voyage.security.user.User

@Repository
interface HealthOrganizationUserRepository extends JpaRepository<HealthOrganizationUser, HealthOrganizationUserKey> {
    List<HealthOrganizationUser> findByUserAndUserIsDeletedFalseAndHealthOrganizationIsDeletedFalse(User user)
    HealthOrganizationUser       findByUserAndUserIsDeletedFalseAndHealthOrganizationAndHealthOrganizationIsDeletedFalse(User user, HealthOrganization healthOrganization)
}
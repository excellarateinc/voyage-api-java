package voyage.connectedhealth.healthorg

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HealthOrganizationRepository extends JpaRepository<HealthOrganization, Long> {
    HealthOrganization findByIdAndIsDeletedFalse(long id)
    Page<HealthOrganization> findByIsDeletedFalse(Pageable pageable)
}
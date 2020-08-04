package voyage.connectedhealth.result

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import voyage.connectedhealth.healthorg.HealthOrganization
import voyage.security.user.User

@Repository
interface ResultRepository extends JpaRepository<Result, Long> {
    Result findByIdAndIsDeletedFalse(long id)
    Page<Result> findByUserAndCreatedDateGreaterThanAndIsDeletedFalse(User user, Date asOfDate, Pageable pageable)
    Page<Result> findByUserAndHealthOrganizationAndCreatedDateGreaterThanAndIsDeletedFalse(User user, HealthOrganization healthOrganization, Date asOfDate, Pageable pageable)
    Page<Result> findByUserAndIsDeletedFalse(User user, Pageable pageable)
    Page<Result> findByUserAndHealthOrganizationAndIsDeletedFalse(User user, HealthOrganization healthOrganization, Pageable pageable)
    Page<Result> findByUserAndResultTypeAndIsDeletedFalse(User user, ResultType resultType, Pageable pageable)
    Page<Result> findByUserAndHealthOrganizationAndResultTypeAndIsDeletedFalse(User user, HealthOrganization healthOrganization, ResultType resultType, Pageable pageable)
}

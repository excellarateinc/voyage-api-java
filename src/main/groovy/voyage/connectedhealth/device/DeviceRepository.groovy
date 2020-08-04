package voyage.connectedhealth.device

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository extends JpaRepository<Device, Long> {
    Page<Device> findByIsDeletedFalse(Pageable pageable)
    Device findByIdAndIsDeletedFalse(long id)
    Page<Device> findByResultTypeIdAndIsDeletedFalseOrderByName(long resultTypeId, Pageable pageable)
}
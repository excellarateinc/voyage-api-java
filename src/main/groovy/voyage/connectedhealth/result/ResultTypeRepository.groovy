package voyage.connectedhealth.result

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResultTypeRepository extends JpaRepository<ResultType, Long> {
}
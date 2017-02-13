package voyage.security.audit

import org.springframework.data.repository.CrudRepository

interface ActionLogRepository extends CrudRepository<ActionLog, Long> {
    
}

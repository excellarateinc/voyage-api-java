package voyage.connectedhealth.reminder

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import voyage.security.user.User

@Repository
interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUser(User user, Pageable pageable)
}
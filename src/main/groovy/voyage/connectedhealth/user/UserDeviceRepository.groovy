package voyage.connectedhealth.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import voyage.security.user.User

@Repository
interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    UserDevice findByIdAndIsDeletedFalse(long id)
    List<UserDevice> findByUserAndIsDeletedFalse(User user)
}
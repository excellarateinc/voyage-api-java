package voyage.security.audit

import org.hibernate.envers.RevisionListener
import voyage.security.user.UserService

/**
 * Hibernate Envers - RevisionListener override that looks up the User within the current security context and injects
 * the value into the RevisionEntity object.
 */
class UserRevisionListener implements RevisionListener {
    @Override
    void newRevision(Object revisionEntity) {
        UserRevisionEntity userRevisionEntity = (UserRevisionEntity)revisionEntity
        userRevisionEntity.username = UserService.currentUsername
        userRevisionEntity.createdDate = new Date()
    }
}

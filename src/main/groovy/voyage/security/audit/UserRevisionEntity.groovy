package voyage.security.audit

import org.hibernate.envers.DefaultRevisionEntity
import org.hibernate.envers.RevisionEntity

import javax.persistence.Entity
import javax.persistence.Table

/**
 * Hibernate Envers extension class that adds the username to the revision transaction table
 */
@Entity
@Table(name='AUD_REVISION')
@RevisionEntity(UserRevisionListener)
class UserRevisionEntity extends DefaultRevisionEntity {
    String username
    Date createdDate
}

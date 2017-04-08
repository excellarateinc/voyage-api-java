package voyage.security.client

import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Audited
@EqualsAndHashCode(includes=['client','grantType'], callSuper=true)
class ClientGrant extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name='client_id')
    Client client

    @Enumerated(EnumType.STRING)
    GrantType grantType
}

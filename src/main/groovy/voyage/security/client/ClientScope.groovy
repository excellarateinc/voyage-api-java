package voyage.security.client

import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Audited
@EqualsAndHashCode(includes=['client','clientScopeType'], callSuper=true)
class ClientScope extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name='client_id')
    Client client

    @ManyToOne
    @JoinColumn(name='client_scope_type_id')
    ClientScopeType clientScopeType
}

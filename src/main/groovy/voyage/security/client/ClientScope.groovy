package voyage.security.client

import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Audited
class ClientScope extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name='client_id')
    Client client

    @ManyToOne
    @JoinColumn(name='client_scope_type_id')
    ClientScopeType clientScopeType
}

package launchpad.security.client

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class ClientGrant {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @ManyToOne
    @JoinColumn(name="client_id")
    Client client

    @Enumerated(EnumType.STRING)
    GrantType grantType
}

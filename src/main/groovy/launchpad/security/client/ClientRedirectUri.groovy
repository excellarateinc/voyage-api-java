package launchpad.security.client

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
class ClientRedirectUri {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @ManyToOne
    @JoinColumn(name="client_id")
    Client client

    @NotNull
    String uri
}

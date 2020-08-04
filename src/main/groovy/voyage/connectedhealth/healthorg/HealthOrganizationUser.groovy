package voyage.connectedhealth.healthorg

import groovy.transform.EqualsAndHashCode
import voyage.security.user.User

import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.validation.constraints.NotNull

@Entity
@EqualsAndHashCode(includes=['id'])
class HealthOrganizationUser implements Serializable {
    @EmbeddedId
    HealthOrganizationUserKey id

    @NotNull
    @ManyToOne
    @MapsId('health_organization_id')
    @JoinColumn(name = 'health_organization_id')
    HealthOrganization healthOrganization

    @NotNull
    @ManyToOne
    @MapsId('id.user_id')
    @JoinColumn(name = 'user_id')
    User user

    @NotNull
    Boolean isPrimary

    @NotNull
    Boolean isShareData

    @NotNull
    Date asOfDate
}

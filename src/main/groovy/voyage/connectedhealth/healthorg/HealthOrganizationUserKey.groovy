package voyage.connectedhealth.healthorg

import groovy.transform.EqualsAndHashCode

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
@EqualsAndHashCode(includes=['healthOrganizationId', 'userId'])
class HealthOrganizationUserKey implements Serializable {
    @Column(name = 'health_organization_id')
    long healthOrganizationId

    @Column(name = 'user_id')
    long userId
}

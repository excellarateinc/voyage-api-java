package voyage.security.audit

import groovy.transform.EqualsAndHashCode
import voyage.security.client.Client
import voyage.security.user.User

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
@EqualsAndHashCode
class ActionLog {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String clientIpAddress

    @NotNull
    String clientProtocol

    @NotNull
    String url

    @NotNull
    String httpMethod

    String httpStatus

    String username

    @ManyToOne
    @JoinColumn(name='user_id')
    User user

    @ManyToOne
    @JoinColumn(name='client_id')
    Client client

    Long durationMs

    String requestHeaders

    String requestBody

    String responseHeaders

    String responseBody

    @NotNull
    Date createdDate

    @NotNull
    Date lastModifiedDate
}
